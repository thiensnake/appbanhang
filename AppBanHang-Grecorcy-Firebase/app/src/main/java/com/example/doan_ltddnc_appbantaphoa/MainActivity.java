package com.example.doan_ltddnc_appbantaphoa;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.doan_ltddnc_appbantaphoa.Model.MyCart;
import com.example.doan_ltddnc_appbantaphoa.Model.MyOrder;
import com.example.doan_ltddnc_appbantaphoa.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirestoreRegistrar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    Toolbar toolbar ;
    DrawerLayout drawerLayout ;
    NavigationView navigationView ;
    AppBarConfiguration appBarConfiguration;
    ActionBarDrawerToggle actionBarDrawerToggle ;
    NavController navController;
    FirebaseDatabase fdatabase ;
    FirebaseAuth auth ;
    FirebaseFirestore db ;
    ArrayList<MyCart> myCartArrayList ;
    ArrayList<MyOrder>myOrderArrayList;
    int mCartItemCount = 0  ;
    Menu getMenu;
    TextView tvEmail;
    TextView tvFullname;
    String UID;
    CircleImageView imgIconUser;
    TextView textCartItemCount ,textCountOrder ;
    int mOrderItemCount =0 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout =findViewById(R.id.drawerlayout);
        navigationView=findViewById(R.id.navigation_view);
        auth =FirebaseAuth.getInstance() ;
        db =FirebaseFirestore.getInstance();
        actionBarDrawerToggle =new ActionBarDrawerToggle(MainActivity.this ,
                drawerLayout,toolbar,R.string.open ,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        appBarConfiguration =new AppBarConfiguration.Builder(R.id.homeFragment,R.id.profileFragment,
                R.id.categoryFragment,R.id.offersFragment,R.id.newProductsFragment,R.id.myOrdersFragment,R.id.myCartFragment).setDrawerLayout(drawerLayout).build();
        navController = Navigation.findNavController(this , R.id.nav_host);
        NavigationUI.setupActionBarWithNavController(this ,navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView ,navController);
        //header
        View view=navigationView.getHeaderView(0);
        tvEmail =view.findViewById(R.id.tvEmail);
        tvFullname=view.findViewById(R.id.tvFullname);
        imgIconUser =view.findViewById(R.id.imgIconUser);
        UID =auth.getCurrentUser().getUid() ;
        loadProfile();

        //count order
        RelativeLayout customLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.count_orders, null);
        textCountOrder = (customLayout.findViewById(R.id.counter));
        navigationView.getMenu().findItem(R.id.myOrdersFragment).setActionView(customLayout);
        processOrder();

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {

                if(navDestination.getId()==R.id.homeFragment){
                    processCart();
                }
                if(navDestination.getId() ==R.id.myOrdersFragment || navDestination.getId()==R.id.myCartFragment ||navDestination.getId()==R.id.profileFragment){
                    getMenu.findItem(R.id.myCartFragment).setVisible(false);

                } else if(getMenu !=null){
                    getMenu.findItem(R.id.myCartFragment).setVisible(true);
                    processCart();
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.homeFragment:
                        break;
                    case R.id.profileFragment:
                        break;
                    case R.id.categoryFragment:
                        break;
                    case R.id.offersFragment:
                        break;
                    case R.id.newProductsFragment:
                        break;
                    case R.id.myOrdersFragment:
                        break;
                    /*case R.id.myCartFragment:
                        break;*/
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return NavigationUI.onNavDestinationSelected(item,navController);
            }
        });
    }

    private void processOrder() {
        db.collection("CurrentUser").document(auth.getCurrentUser().getUid()).collection("MyOrder").addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                myOrderArrayList =new ArrayList<>();
                if(value!=null){
                    for(DocumentSnapshot documentSnapshot :value.getDocuments()){
                        MyOrder myOrder =documentSnapshot.toObject(MyOrder.class);
                        if (myOrder.getStatus()==0) {
                            myOrderArrayList.add(myOrder);
                        }
                    }
                    mOrderItemCount =myOrderArrayList.size();
                    textCountOrder.setText(mOrderItemCount+"");
                    setupCountOrder();

                }
            }
        });
    }

    public void processCart() {

        db.collection("CurrentUser").document(auth.getCurrentUser().getUid()).collection("MyCart").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        myCartArrayList =new ArrayList<>();
                        if(task.isComplete()){
                            for(QueryDocumentSnapshot ducument : task.getResult()){
                                MyCart myCart =ducument.toObject(MyCart.class);
                                myCartArrayList.add(myCart);
                            }
                            mCartItemCount= myCartArrayList.size();
                            textCartItemCount.setText(mCartItemCount+"");
                            setupBadge();
                        }
                    }
                });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenu =menu;
        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.item_cart,menu);
        MenuItem menuItem=menu.findItem(R.id.myCartFragment);
        MenuItemCompat.setActionView(menuItem,R.layout.menu_cart_custom);
        FrameLayout frameView = (FrameLayout) MenuItemCompat.getActionView(menuItem);
        textCartItemCount =frameView.findViewById(R.id.tv_count_cart);

        frameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(getMenu.findItem(R.id.myCartFragment));
            }

        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() ==R.id.myCartFragment && mCartItemCount >0){
            return NavigationUI.onNavDestinationSelected(item,navController);
        }
        return false ;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController ,appBarConfiguration)||super.onSupportNavigateUp();
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
    public void setupBadge(){
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount,
                        99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    public void setupCountOrder(){
        if (textCountOrder != null) {
            if (mOrderItemCount == 0) {
                if (textCountOrder.getVisibility() != View.GONE) {
                    textCountOrder.setVisibility(View.GONE);
                }
            } else {
                textCountOrder.setText(String.valueOf(Math.min(mOrderItemCount,
                        99)));
                if (textCountOrder.getVisibility() != View.VISIBLE) {
                    textCountOrder.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    public void loadProfile(){
        fdatabase =FirebaseDatabase.getInstance();
        fdatabase.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user =snapshot.getValue(User.class);
                        Glide.with(MainActivity.this ).load(user.getProfileImg()).into(imgIconUser);
                        tvEmail.setText(user.getEmail());
                        tvFullname.setText(user.getFirstname() +" "+user.getLastname());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        processCart();
        loadProfile();
    }

}