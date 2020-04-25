package com.example.caricuan.Transactions.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caricuan.R;
import com.example.caricuan.Transactions.SingleTransactionActivity;
import com.example.caricuan.Transactions.Transaction;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ReceivableFragment extends Fragment {

    // The UI attributes
    private RecyclerView mTransactionList;
    // Adapter for RecyclerView
    private FirebaseRecyclerOptions<Transaction> options;
    private FirebaseRecyclerAdapter<Transaction, TransactionViewHolder> firebaseRecyclerAdapter;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseCatalog;
    private Query mDatabaseReceivable;

    // Empty constructor
    public ReceivableFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transaction_list, container, false);

        // Make the XML components to java objects
        // Set up the RecyclerView
        mTransactionList = rootView.findViewById(R.id.transaction_list);
        mTransactionList.setHasFixedSize(true);
        mTransactionList.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseReceivable = FirebaseDatabase.getInstance().getReference().child("Transactions")
                .orderByChild("seller").equalTo(mAuth.getCurrentUser().getUid());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set up the adapter for RecyclerView
        options = new FirebaseRecyclerOptions.Builder<Transaction>()
                .setQuery(mDatabaseReceivable, Transaction.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Transaction, TransactionViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TransactionViewHolder holder, int position, @NonNull final Transaction model) {

                final String transactionKey = getRef(position).getKey(); // Get the transaction id from firebase

                // Set up the views inside CardView
                holder.setIcon(model.getSeller());
                holder.setTitle(model.getCatalog_id());
                holder.setUsername(model.getSeller(), model.getBuyer());
                holder.setStatus(model.isConfirmed());
                holder.setDate(model.getDate());
                holder.setAlert(model.isRejected(), model.isConfirmed(), model.getReceipt(), model.getSeller());

                // Make every single CardView clickable
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Go to SingleTransactionActivity
                        getActivity().finish();
                        Intent singleTransactionIntent = new Intent(getActivity(), SingleTransactionActivity.class);
                        singleTransactionIntent.putExtra("transactionId", transactionKey);
                        startActivity(singleTransactionIntent);

                    }
                });

            }

            @NonNull
            @Override
            public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_row, parent, false);
                return new TransactionViewHolder(view);
            }

        };
        mTransactionList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}
