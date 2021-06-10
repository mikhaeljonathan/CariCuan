package cari.cuan.bli.Entries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cari.cuan.bli.R;

public class FirstActivity extends AppCompatActivity {

    private ViewPager viewPager; // To make slider
    private int[] layouts; // To store the pictures
    private Adapter adapter; // To populate the slider

    private Button mLoginBtn; // To login
    private Button mSignupBtn; // To signup

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // Set up the slider
        viewPager = findViewById(R.id.first_pager);
        layouts = new int[] { //Here is the layouts of the slider
                R.layout.slider1,
                R.layout.slider2,
                R.layout.slider3,
                R.layout.slider4,
                R.layout.slider5
        };
        adapter = new Adapter(this, layouts);
        viewPager.setAdapter(adapter);

        // Make the XML components to java objects
        mLoginBtn = findViewById(R.id.first_login_button);
        mSignupBtn = findViewById(R.id.first_signup_button);

        // Login button clicked, go to LoginActivity
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to LoginActivity
                finish();
                startActivity(new Intent(FirstActivity.this, LoginActivity.class));

            }
        });

        // Signup button clicked, go to SignupActivity
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to SignupActivity
                finish();
                startActivity(new Intent(FirstActivity.this, SignupActivity.class));
            }
        });
    }

    // To prevent going back to MainActivity
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // Set the Adapter of the viewPager
    public class Adapter extends PagerAdapter {

        int[] layouts;
        LayoutInflater layoutInflater;

        public Adapter (Context context, int[] layouts){
            this.layouts = layouts;
            this.layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE); //Inflate the layout
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View)object);
        }
    }
}
