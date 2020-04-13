package ba.unsa.etf.rma.rma20siljakamina96.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ba.unsa.etf.rma.rma20siljakamina96.R;

public class BudgetFragment extends Fragment implements IAccountView{
    private TextView budgetText;
    private IAccountPresenter accountPresenter;

    public IAccountPresenter getAccountPresenter() {
        if (accountPresenter == null) {
            accountPresenter = new AccountPresenter(this, getActivity());
        }
        return accountPresenter;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_budget, container, false);
        budgetText = (TextView)fragmentView.findViewById(R.id.budgetView);
        budgetText.setText(String.valueOf(getAccountPresenter().getAccount().getBudget()));
        return fragmentView;
    }
}