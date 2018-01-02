package com.crypto.amartino.mycryptobalance;

/**
 * Created by Caterina on 02/01/2018.
 */

public class Coin {
    private String name, amount, total;

    public Coin() {
    }

    public Coin(String name, String amount, String total) {
        this.name = name;
        this.amount = amount;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
