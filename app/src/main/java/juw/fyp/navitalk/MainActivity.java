package juw.fyp.navitalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment Home = new HomeFragment();
    private SettingsFragment settings = new SettingsFragment();
    private BottomNavigationView menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       setFragment(Home);
        menu=findViewById(R.id.menu_item);
        menu.setSelectedItemId(R.id.menu_home);

        menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.isChecked()){
                    return true;
                } else {
                    switch (item.getItemId()) {
                        case R.id.menu_home:
                            setFragment(Home);
                            getSupportActionBar().setTitle("NaviTalk");
                            return true;

                        case R.id.menu_setting:
                            setFragment(settings);
                          getSupportActionBar().setTitle("Settings");
                            return true;

                        default:
                            setFragment(Home);
                            getSupportActionBar().setTitle("NaviTalk");
                            return true;
                    }}
            }
        });


    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame , fragment);
        ft.commit();
    }

}