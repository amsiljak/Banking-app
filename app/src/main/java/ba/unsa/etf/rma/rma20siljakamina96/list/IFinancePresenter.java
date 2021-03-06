package ba.unsa.etf.rma.rma20siljakamina96.list;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;

import ba.unsa.etf.rma.rma20siljakamina96.data.Account;
import ba.unsa.etf.rma.rma20siljakamina96.data.Transaction;

public interface IFinancePresenter {
//    void refresh();

    ArrayList<Transaction> sortTransactions(ArrayList<Transaction> transactions);
    ArrayList<Transaction> filterTransactionsByType(ArrayList<Transaction> transactions);
    ArrayList<Transaction> filterTransactionsByDate(ArrayList<Transaction> transactions);

    void getTransactions(String type, String sort, Calendar cal);

    void setAccount();

    Account getAccount();


    void undoAction(Transaction transaction);

    boolean isOnline(Context context);


    //    void setTransactions();
//    void setAccount();

}
