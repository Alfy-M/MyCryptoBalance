package com.crypto.amartino.mycryptobalance;

/**
 * Created by Caterina on 02/01/2018.
 */


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;




public class CoinsAdapter extends RecyclerView.Adapter<CoinsAdapter.MyViewHolder> {


    private List<Coin> coinsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, amount, total;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            amount = (TextView) view.findViewById(R.id.amount);
            total = (TextView) view.findViewById(R.id.total);
        }
    }


    public CoinsAdapter(List<Coin> coinsList) {
        this.coinsList = coinsList;
    }

    public List<Coin> getCoinsList() {
        return coinsList;
    }

    public void setCoinsList(List<Coin> coinsList) {
        this.coinsList = coinsList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coin_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Coin coin = coinsList.get(position);
        holder.name.setText(coin.getName());
        holder.amount.setText(coin.getAmount());
        holder.total.setText(coin.getTotal());
    }

    @Override
    public int getItemCount() {
        return coinsList.size();
    }
}