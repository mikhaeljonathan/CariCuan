package cari.cuan.bli.Transactions;

public class Transaction {
    private String catalog_id;
    private String buyer;
    private String seller;
    private String date;
    private int stocks;
    private double total_price;
    private boolean rejected;
    private boolean confirmed;
    private String receipt;

    public Transaction(){}

    public Transaction(String catalog_id, String buyer, String seller, String date, int stocks
            , double total_price, boolean rejected, boolean confirmed, String receipt) {

        this.catalog_id = catalog_id;
        this.buyer = buyer;
        this.seller = seller;
        this.date = date;
        this.stocks = stocks;
        this.total_price = total_price;
        this.rejected = rejected;
        this.confirmed = confirmed;
        this.receipt = receipt;

    }

    public String getCatalog_id() {
        return catalog_id;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public String getDate() {
        return date;
    }

    public int getStocks() {
        return stocks;
    }

    public double getTotal_price() {
        return total_price;
    }

    public boolean isRejected() {return rejected;}

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setCatalog_id(String catalog_id) {
        this.catalog_id = catalog_id;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStocks(int stocks) {
        this.stocks = stocks;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setRejected(boolean rejected) {this.rejected = rejected;}

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }
}
