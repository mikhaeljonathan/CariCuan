package cari.cuan.bli.MainFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cari.cuan.bli.Catalogs.SellActivity;
import cari.cuan.bli.Catalogs.Catalog;
import cari.cuan.bli.Catalogs.SingleCatalogActivity;
import cari.cuan.bli.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FeedsFragment extends Fragment {

    // The UI attributes
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private RecyclerView mCatalogList;
    // Adapter for RecyclerView
    private FirebaseRecyclerOptions<Catalog> options;
    private FirebaseRecyclerAdapter<Catalog, CatalogViewHolder> firebaseRecyclerAdapter;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseCatalog;
    private Query mDatabaseCatalogQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);

        // Make the XML components to java objects
        // Set up the toolbar
        setHasOptionsMenu(true);
        toolbar = rootView.findViewById(R.id.feed_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        // Set up the floating action button
        fab = rootView.findViewById(R.id.catalog_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to SellActivity
                getActivity().finish();
                startActivity(new Intent(getActivity(), SellActivity.class));

            }
        });

        // Set up the RecyclerView
        mCatalogList = rootView.findViewById(R.id.feed_list);
        mCatalogList.setHasFixedSize(true);
        mCatalogList.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseCatalogQuery = FirebaseDatabase.getInstance().getReference().child("Catalogues").orderByChild("order");

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set up the adapter for RecyclerView
        options = new FirebaseRecyclerOptions.Builder<Catalog>()
                .setQuery(mDatabaseCatalogQuery, Catalog.class).build(); // Set the query
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Catalog, CatalogViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CatalogViewHolder holder, int position, @NonNull Catalog model) {

                final String catalogKey = getRef(position).getKey(); // Get the catalog id from firebase

                // Set up the views inside CardView
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDesc());
                holder.setImage(model.getImages());
                holder.setImageUser(model.getUser());
                holder.setUser(model.getUser());
                holder.setClasses(model.getClasses());
                holder.setStock(model.getStocks());
                holder.setPrice(model.getPrice());

                // Make every single CardView clickable
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Go to SingleCatalogActivity
                        getActivity().finish();
                        Intent singleCatalogIntent = new Intent(getActivity(), SingleCatalogActivity.class);
                        singleCatalogIntent.putExtra("catalogId", catalogKey);
                        startActivity(singleCatalogIntent);

                    }
                });

            }

            @NonNull
            @Override
            public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_row, parent, false);
                return new CatalogViewHolder(view);
            }

        };
        mCatalogList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    // A ViewHolder class for supporting the adapter
    public static class CatalogViewHolder extends RecyclerView.ViewHolder {

        View mView; // View inside catalog_row.xml
        DatabaseReference mDatabaseUser;

        public CatalogViewHolder(@NonNull View itemView) {
            super(itemView);

            // Setting the view of ViewHolder
            mView = itemView;
            mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        }

        public void setTitle(String title){

            TextView feedTitle = mView.findViewById(R.id.feed_title);
            feedTitle.setText(title);

        }

        public void setDesc(String desc){

            TextView feedDesc = mView.findViewById(R.id.feed_desc);
            feedDesc.setText(desc);

        }

        public void setImage(String image){

            ImageView feedImage = mView.findViewById(R.id.feed_image);
            Picasso.get().load(Uri.parse(image)).into(feedImage);

        }

        public void setImageUser(String user){

            mDatabaseUser.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ImageView feedImageUser = mView.findViewById(R.id.single_catalog_user_pic);
                    // Parse it to an ImageView
                    Picasso.get().load(Uri.parse(dataSnapshot.child("image").getValue().toString())).into(feedImageUser);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setUser(String user){

            mDatabaseUser.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    TextView feedUsername = mView.findViewById(R.id.single_catalog_username);
                    feedUsername.setText(dataSnapshot.child("name").getValue().toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        public void setClasses(String classes) {

            TextView feedClass = mView.findViewById(R.id.single_catalog_class);
            feedClass.setText(classes);

        }

        public void setStock(int stock){

            TextView feedStock = mView.findViewById(R.id.single_catalog_stock);
            feedStock.setText(stock + " stock(s) left");

        }

        public void setPrice(double price){

            TextView feedPrice = mView .findViewById(R.id.single_catalog_price);
            feedPrice.setText("Rp " + price);

        }

    }

    // For the toolbar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    // For the toolbar's single item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_search:
                Toast.makeText(getActivity(), "Under maintenance", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_sort:
                return true;

            case R.id.sort_all:
                mDatabaseCatalogQuery = FirebaseDatabase.getInstance().getReference().child("Catalogues").orderByChild("order");
                break;
            case R.id.sort_mine:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("user").equalTo(mAuth.getCurrentUser().getUid());
                break;

            // Sort the query based on the class
            case R.id.sort_PPTI8:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPTI 8");
                break;
            case R.id.sort_PPTI7:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPTI 7");
                break;
            case R.id.sort_PPTI6:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPTI 6");
                break;
            case R.id.sort_PPA51:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPA 51");
                break;
            case R.id.sort_PPA49:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPA 49");
                break;
            case R.id.sort_PPA48:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPA 48");
                break;
            case R.id.sort_PPA47:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPA 47");
                break;
            case R.id.sort_PPA46:
                mDatabaseCatalogQuery = mDatabaseCatalog.orderByChild("classes").equalTo("PPA 46");
                break;
        }

        onStart();
        return super.onOptionsItemSelected(item);
    }

}
