package cari.cuan.bli.Transactions.List;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cari.cuan.bli.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// This is a class to supporting the RecyclerView Adapter
public class TransactionViewHolder extends RecyclerView.ViewHolder {

    View mView; // View inside transaction_row.xml
    DatabaseReference mDatabaseUser;
    DatabaseReference mDatabaseCatalog;

    public TransactionViewHolder(@NonNull View itemView) {
        super(itemView);

        // Setting the view of ViewHolder
        mView = itemView;
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");

    }

    public void setIcon(String seller){

        ImageView icon = mView.findViewById(R.id.transaction_icon);
        if (seller.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ // Current user is a seller

            icon.setImageResource(R.drawable.ic_receivable_black_24dp);

        } else { // Current user is a buyer

            icon.setImageResource(R.drawable.ic_payable_black_24dp);

        }

    }

    public void setTitle(final String catalogId){

        final TextView title = mView.findViewById(R.id.transaction_title);

        // Retrieve data from Catalogues database
        mDatabaseCatalog.child(catalogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                title.setText(dataSnapshot.child("title").getValue().toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void setUsername(String seller, String buyer){

        final TextView username = mView.findViewById(R.id.transaction_seller);

        if (seller.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ // Current user is a seller

            // Retrieve data from user database
            mDatabaseUser.child(buyer).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    username.setText("Buyer: " + dataSnapshot.child("name").getValue().toString()
                            + " - " + dataSnapshot.child("classes").getValue().toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else { // Current user is a buyer

            // Retrieve data from user database
            mDatabaseUser.child(seller).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    username.setText("Seller: " + dataSnapshot.child("name").getValue().toString()
                            + " - " + dataSnapshot.child("classes").getValue().toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    public void setStatus(boolean confirmed){

        TextView status = mView.findViewById(R.id.transaction_status);

        if (confirmed){ // Completed transaction

            status.setText("Completed transaction");
            status.setTextColor(Color.parseColor("#00FF00"));
            status.setTypeface(Typeface.DEFAULT_BOLD);

        } else { // Uncompleted transaction

            status.setText("Transaction hasn't completed yet");
            status.setTextColor(Color.parseColor("#FF0000"));

        }
    }

    public void setDate(String date){
        TextView dateTv = mView.findViewById(R.id.transaction_date);
        dateTv.setText(date);
    }

    public void setAlert(boolean rejected, boolean confirmed, String receipt, String seller){

        final ImageView alert = mView.findViewById(R.id.transaction_alert);
        alert.setVisibility(View.GONE); // Alert picture hasn't shown yet

        if (seller.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){ // Current user is a seller

            // Seller hasn't rejected and there is a receipt and is not confirmed yet
            if (!rejected && !receipt.equals("nothing") && !confirmed) {

                alert.setVisibility(View.VISIBLE); // Show the alert picture

            }

        } else { // Current user is a buyer

            // The receipt hasn't uploaded yet or the receipt is rejected by the seller
            if (rejected || receipt.equals("nothing")) {

                alert.setVisibility(View.VISIBLE); // Show the alert picture

            }

        }

    }

}
