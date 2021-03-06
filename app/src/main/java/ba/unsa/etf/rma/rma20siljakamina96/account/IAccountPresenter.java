package ba.unsa.etf.rma.rma20siljakamina96.account;

import ba.unsa.etf.rma.rma20siljakamina96.data.Account;

public interface IAccountPresenter {

    Account getAccount();

    void modifyAccount(double budget, double totalLimit, double monthLimit);

    void setAccountData();

    void uploadToServis();

    boolean accountChanged();
}
