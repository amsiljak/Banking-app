package ba.unsa.etf.rma.rma20siljakamina96.list;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.unsa.etf.rma.rma20siljakamina96.data.Transaction;
import ba.unsa.etf.rma.rma20siljakamina96.data.Type;
import ba.unsa.etf.rma.rma20siljakamina96.util.TransactionDBOpenHelper;

import static ba.unsa.etf.rma.rma20siljakamina96.util.TransactionDBOpenHelper.TRANSACTION_ID;
import static ba.unsa.etf.rma.rma20siljakamina96.util.TransactionDBOpenHelper.TRANSACTION_INTERNAL_ID;

public class TransactionListInteractor extends AsyncTask<String, Integer, Void> implements ITransactionInteractor {

    private OnTransactionsGetDone caller;
    private static ArrayList<Transaction> transactions = new ArrayList<>();
    public static Map<Integer,String> transactionTypes;
    private String key = "a8dfa9fe-fe66-4026-9fb0-1c6abcdd0f10";

    private TransactionDBOpenHelper transactionDBOpenHelper;
    SQLiteDatabase database;

    public static ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public TransactionListInteractor(OnTransactionsGetDone p) {
        caller = p;
    };
    public TransactionListInteractor() {};

    public static void removeFromListOfTransactions(int id) {
        for(Transaction t:transactions) {
            if(t.getId() == id) {
                transactions.remove(t);
                break;
            }
        }
    }
    public static void addToListOfTransactions(Transaction t) {
        transactions.add(t);
    }

    public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }
    private String getQuery(String type, String sort, String month, String year) {
        String query = "";

        if(type != null && !type.equals("All")){
            for (Map.Entry<Integer,String> entry : transactionTypes.entrySet()) {
                if(type.equals(entry.getValue())) { //ako nadje ime tipa transakcije iz filter liste u query stavi id tog tipa
                    type = entry.getKey().toString();
                    break;
                }
            }
            query += "typeId=" + type;
        }
        if(query != "") query+= "&";

        if (sort != null) {
            if(sort.equals("Price - Ascending")) sort = "amount.asc";
            else if(sort.equals("Price - Descending"))  sort = "amount.desc";
            else if(sort.equals("Title - Ascending"))  sort = "title.asc";
            else if(sort.equals("Title - Descending"))  sort = "title.desc";
            else if(sort.equals("Date - Ascending"))  sort = "date.asc";
            else if(sort.equals("Date - Descending")) sort = "date.desc";
            query += "sort=" + sort;
        }
        if(query != "") query+= "&";

        if (month != null) {
            if(month.length() == 1) month = '0' + month;
            query += "month=" + month;
        }
        if(query != "") query+= "&";
        if (year != null) {
            query += "year=" + year;
        }
        return query;
    }
    @Override
    protected Void doInBackground(String... strings) {
        List<Transaction> temp = new ArrayList<>();
        AddTypes();
        int page = 0;

        String query = getQuery(strings[0],strings[1],strings[2],strings[3]);

        while(true) {
            String url1;
            if(query == "") {
                if (page == 0)
                    url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/"+key+"/transactions";
                else
                    url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/" + key + "/transactions?page=" + page;
            }
            else {
                if (page == 0)
                    url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/" + key + "/transactions/filter?"+query;
                else
                    url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/account/" + key + "/transactions/filter?page=" + page+"&"+query;
            }
            try {
                SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                URL url = new URL(url1);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String result = convertStreamToString(in);
                JSONObject jo = new JSONObject(result);
                JSONArray results = jo.getJSONArray("transactions");
                if(results.length() == 0) break;

                for (int i = 0; i < results.length(); i++) {

                    JSONObject transaction = results.getJSONObject(i);

                    Integer id = transaction.getInt("id");
                    Date date = DATE_FORMAT.parse(transaction.getString("date"));
                    String title = transaction.getString("title");
                    Double amount = transaction.getDouble("amount");
                    String itemDescription = transaction.getString("itemDescription");
                    int transactionTypeId = transaction.getInt("TransactionTypeId");

                    Integer transactionInterval = 0;
                    if (!transaction.isNull("transactionInterval")) {
                        transactionInterval = Integer.valueOf(transaction.getString("transactionInterval"));
                    }

                    Date endDate = null;
                    if (!transaction.isNull("endDate")) {
                        endDate = DATE_FORMAT.parse(transaction.getString("endDate"));
                    }
                    String type = "";
                    for (Map.Entry<Integer,String> entry : transactionTypes.entrySet()) {
                        if(transactionTypeId == entry.getKey()) {
                            type = entry.getValue();
                            break;
                        }
                    }
                    temp.add(new Transaction(id, date, amount, title, Type.valueOf(type), itemDescription, transactionInterval, endDate));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            page++;
        }
        transactions = new ArrayList<>();
        transactions.addAll(temp);
        return null;
    }

    protected Void AddTypes(String... params)
    {
        transactionTypes = new HashMap<>();
        try {
            URL url = null;
            String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com/transactionTypes";
            try {
                url = new URL(url1);
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rezultat = convertStreamToString(in);
            JSONObject jo = new JSONObject(rezultat);

            JSONArray items = jo.getJSONArray("rows");
            for (int i = 0; i < items.length(); i++) {
                JSONObject type = items.getJSONObject(i);
                Integer id = type.getInt("id");
                String name = type.getString("name");

                if (!transactionTypes.containsKey(id)) {
                    transactionTypes.put(id, name.replaceAll("\\s","").toUpperCase());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return null;
    }


    public interface OnTransactionsGetDone {
        public void onTransactionsGetDone(ArrayList<Transaction> results);
    }
    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        caller.onTransactionsGetDone(transactions);
    }

    private String getQuery(String change) {
        String query = "SELECT *"  + " FROM "
                + TransactionDBOpenHelper.TRANSACTION_TABLE + " WHERE " + TransactionDBOpenHelper.TRANSACTION_CHANGE + " = '"+change+"'";

        return query;
    }
    @Override
    public ArrayList<Transaction> getDeletedTransactions(Context context) {
        return getTransactions("delete",context);
    }
    @Override
    public ArrayList<Transaction> getModifiedTransactions(Context context) {
        return getTransactions("modify", context);
    }
    @Override
    public ArrayList<Transaction> getAddedTransactions(Context context) {
        return getTransactions("add", context);
    }


    private ArrayList<Transaction> getTransactions(String change, Context context) {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ArrayList<Transaction> transactions= new ArrayList<>();
//        ContentResolver cr = context.getApplicationContext().getContentResolver();
//        String[] kolone = null;
//        Uri adresa = ContentUris.withAppendedId(Uri.parse(""),id
//        );

        transactionDBOpenHelper = new TransactionDBOpenHelper(context);
        database = transactionDBOpenHelper.getWritableDatabase();
        String query = getQuery(change);
        Cursor cursor = database.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do{
                int idPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_ID);
                //ako je transakcija dodana offline pa nema id sa servera onda cu joj za id
                //postaviti internal id da bi se mogla kasnije pronaci po njemu
                if(cursor.getInt(idPos) == 0) {
                    idPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_INTERNAL_ID);
                }
                int titlePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_TITLE);
                int datePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_DATE);
                int intervalPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_INTERVAL);
                int amountPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_AMOUNT);
                int descriptionPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_DESCRIPTION);
                int typePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_TYPE);
                int endDatePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_ENDDATE);

                String typeString = cursor.getString(typePos);
                Type type = null;
                if(typeString!= null) type = Type.valueOf(typeString);
                Date endDate = null;
                if(cursor.getString(endDatePos) != null) {
                    try {
                        endDate = DATE_FORMAT.parse(cursor.getString(endDatePos));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    transactions.add(new Transaction(cursor.getInt(idPos),DATE_FORMAT.parse(cursor.getString(datePos)),cursor.getDouble(amountPos)
                            ,cursor.getString(titlePos),type,cursor.getString(descriptionPos),cursor.getInt(intervalPos),endDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return transactions;
    }

    //kad se transakciji iz baze zeli modifikovati atribut
    @Override
    public void updateDB(String date, Double amount, String title, String type, String itemDescription, Integer transactionInterval, String endDate, Integer id, Context context, boolean hasRealID) {
        transactionDBOpenHelper = new TransactionDBOpenHelper(context);
        database = transactionDBOpenHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        String where;
        if(hasRealID) where = TRANSACTION_ID + "=" + String.valueOf(id);
        else where = TRANSACTION_INTERNAL_ID + "=" + String.valueOf(id);
        String whereArgs [] = null;
        values.put(transactionDBOpenHelper.TRANSACTION_TITLE,title);
        values.put(transactionDBOpenHelper.TRANSACTION_DATE, date);
        values.put(transactionDBOpenHelper.TRANSACTION_INTERVAL,transactionInterval);
        values.put(transactionDBOpenHelper.TRANSACTION_AMOUNT, amount);
        values.put(transactionDBOpenHelper.TRANSACTION_DESCRIPTION,itemDescription);
        values.put(transactionDBOpenHelper.TRANSACTION_TYPE, type);
        values.put(transactionDBOpenHelper.TRANSACTION_ENDDATE, endDate);

        database.update(transactionDBOpenHelper.TRANSACTION_TABLE, values, where, whereArgs);
        database.close();
    }
    @Override
    public void deleteFromDB(int id, Context context,boolean hasRealID) {
        transactionDBOpenHelper = new TransactionDBOpenHelper(context);
        database = transactionDBOpenHelper.getWritableDatabase();

        String where;
        if(hasRealID) where = TRANSACTION_ID + "=" + id;
        else  where = TRANSACTION_INTERNAL_ID + "=" + id;
        String whereArgs [] = null;

        database.delete(transactionDBOpenHelper.TRANSACTION_TABLE, where, whereArgs);
        database.close();
    }
}
