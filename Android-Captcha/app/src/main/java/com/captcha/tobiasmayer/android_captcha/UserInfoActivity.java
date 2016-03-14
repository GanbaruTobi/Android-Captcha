package com.captcha.tobiasmayer.android_captcha;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class UserInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        String result = getIntent().getExtras().getString("ServerResponse");

        try{
            JSONObject json = new JSONObject(result);
            
            String name = json.getString("name");
            TextView textViewTextName =(TextView) findViewById(R.id.textViewTextName);
            textViewTextName.setText(name);
            
            String surname = json.getString("surname");
            TextView textViewTextSurname = (TextView) findViewById(R.id.textViewTextSurname);
            textViewTextSurname.setText(surname);

            ArrayList<Transaction> transactions = fromJson(json.getJSONArray("transactions"));
            Collections.sort(transactions, new TransactionTimeComparator());
            TransactionsAdapter adapter = new TransactionsAdapter(this, transactions);
            ListView listViewTransactions = (ListView) findViewById(R.id.listViewTransactions);
            listViewTransactions.setAdapter(adapter);

        }catch (JSONException e){
            //TODO
        }
    }

    private ArrayList<Transaction> fromJson(JSONArray jsonObjects) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                transactions.add(new Transaction(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return transactions;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        return DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
    }

    private class Transaction {
        long timestamp;
        String amount;

        private Transaction(JSONObject object) {
            try {

                this.timestamp = object.getLong("tmpstmp");
                this.amount = object.getString("amount");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class TransactionsAdapter extends ArrayAdapter<Transaction> {
        private TransactionsAdapter (Context context, ArrayList<Transaction> transactions) {
            super(context, 0, transactions);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Transaction transaction = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
            }
            TextView textViewTextTimeStamp = (TextView) convertView.findViewById(R.id.tmpstmp);
            TextView textViewTextAmount = (TextView) convertView.findViewById(R.id.amount);
            textViewTextTimeStamp.setText(getDate(transaction.timestamp*1000));
            textViewTextAmount.setText((transaction.amount));
            return convertView;
        }
    }

    private class TransactionTimeComparator implements Comparator<Transaction> {
        public int compare(Transaction left, Transaction right) {
            return Long.valueOf(left.timestamp).compareTo(right.timestamp);
        }
    }
}
