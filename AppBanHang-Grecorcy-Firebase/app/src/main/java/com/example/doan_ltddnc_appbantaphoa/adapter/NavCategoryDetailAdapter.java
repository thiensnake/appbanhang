package com.example.doan_ltddnc_appbantaphoa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doan_ltddnc_appbantaphoa.DetailedActivity;
import com.example.doan_ltddnc_appbantaphoa.LoadingDialog;
import com.example.doan_ltddnc_appbantaphoa.Model.NavCategoryDetailed;
import com.example.doan_ltddnc_appbantaphoa.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NavCategoryDetailAdapter extends RecyclerView.Adapter<NavCategoryDetailAdapter.ViewHolder> {
    Context context ;
    List<NavCategoryDetailed> navCategoryDetaileds ;
    int totalPrice = 0;
    FirebaseAuth auth;
    FirebaseFirestore db;
    public NavCategoryDetailAdapter(Context context, List<NavCategoryDetailed> navCategoryDetaileds) {
        this.context = context;
        this.navCategoryDetaileds = navCategoryDetaileds;
    }

    @NonNull
    @Override
    public NavCategoryDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_category_detail_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NavCategoryDetailAdapter.ViewHolder holder, int position) {
        NavCategoryDetailed navCategoryDetailed = navCategoryDetaileds.get(position);
        Glide.with(context).load(navCategoryDetailed.getImg_url()).into(holder.imgProduct);
        auth=FirebaseAuth.getInstance();
        db =FirebaseFirestore.getInstance();
        holder.tvNameProduct.setText(navCategoryDetailed.getName());
        Locale locale =new Locale("vi","VN");
        NumberFormat numberFormat =NumberFormat.getNumberInstance(locale);
        holder.tvPrice.setText(numberFormat.format(navCategoryDetailed.getPrice())+"đ");
        navCategoryDetailed.setTotalQuantity(1);
        holder.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int totalQuantity = navCategoryDetailed.getTotalQuantity() ;
                if (navCategoryDetailed.getTotalQuantity() < 10) {
                    totalQuantity++ ;
                    navCategoryDetailed.setTotalQuantity(totalQuantity);
                    holder.tvQuantity.setText(String.valueOf(navCategoryDetailed.getTotalQuantity()));
                    totalPrice = navCategoryDetailed.getPrice() * navCategoryDetailed.getTotalQuantity();
                }
            }
        });
        holder.imgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int totalQuantity = navCategoryDetailed.getTotalQuantity() ;
                if (totalQuantity >1) {
                    totalQuantity-- ;
                    navCategoryDetailed.setTotalQuantity(totalQuantity);
                    holder.tvQuantity.setText(String.valueOf(navCategoryDetailed.getTotalQuantity()));
                    totalPrice = navCategoryDetailed.getPrice() * navCategoryDetailed.getTotalQuantity();
                }
            }
        });
        holder.btnBuyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String saveCurrentDate, saveCurrentTime;
                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MM/dd/yyyy");
                saveCurrentDate = currentDate.format(calForDate.getTime());
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
                saveCurrentTime = currentTime.format(calForDate.getTime());
                final HashMap<String, Object> cartMap = new HashMap<>();
                cartMap.put("productName",navCategoryDetailed.getName() );
                cartMap.put("productPrice", navCategoryDetailed.getPrice());
                cartMap.put("currentDate", saveCurrentDate);
                cartMap.put("currentTime", saveCurrentTime);
                cartMap.put("totalQuantity", navCategoryDetailed.getTotalQuantity());
                cartMap.put("totalPrice", totalPrice);
                cartMap.put("img_url",navCategoryDetailed.getImg_url());
                LoadingDialog loadingDialog = new LoadingDialog(context);
                loadingDialog.ShowDialog("Thêm giỏ hàng");

                db.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                        .collection("MyCart").add(cartMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isComplete()) {
                                    loadingDialog.HideDialog();
                                    Toast.makeText(context, "Đã thêm giỏ hàng", Toast.LENGTH_SHORT).show();
                                    holder.tvQuantity.setText("1");
                                    navCategoryDetailed.setTotalQuantity(1);
                                } else {
                                    Toast.makeText(context, "Lỗi", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }


    @Override
    public int getItemCount() {
        return navCategoryDetaileds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct ,imgMinus ,imgPlus ;
        TextView tvNameProduct,tvPrice ,tvQuantity ;
        AppCompatButton btnBuyNow ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct=itemView.findViewById(R.id.img_nav_cartegory_product);
            tvNameProduct=itemView.findViewById(R.id.nav_cat_name_product);
            tvPrice=itemView.findViewById(R.id.nav_cat_price_product);
            imgMinus =itemView.findViewById(R.id.img_nav_cat_minus);
            imgPlus =itemView.findViewById(R.id.img_nav_cat_plus);
            tvQuantity =itemView.findViewById(R.id.tv_nav_cat_TotalQuantity);
            btnBuyNow =itemView.findViewById(R.id.btn_nav_cat_add_cart);
        }
    }
}
