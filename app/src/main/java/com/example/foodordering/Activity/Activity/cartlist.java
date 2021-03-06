package com.example.foodordering.Activity.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodordering.Activity.Adapter.cartviewAdapter;
import com.example.foodordering.Activity.Domain.Cart;
import com.example.foodordering.Activity.Domain.Orders;
import com.example.foodordering.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class cartlist extends AppCompatActivity {
    private ImageView plusBtn, minusBtn;
    private int tableno = 1;
    private TextView tabletxt,overallalltotal,orderqty,fidtxtincart, txtnameofitem ;
    public static Button orderbtn;
    private RecyclerView.LayoutManager layoutManagerger;
    private ScrollView scrollView;
    private TextView emptytxt;
    private RecyclerView recycleview;
    cartviewAdapter cartAdapter;
    ArrayList<Cart> cartArrayList;
    ArrayList<Orders> ordersArrayList;
    public int oid=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartlist);


        initView();
        setTableno();
       // calculatetotalprice();


        recycleview =findViewById(R.id.cartlist);
        recycleview.setHasFixedSize(true);
        layoutManagerger =new LinearLayoutManager(this);
        recycleview.setLayoutManager(layoutManagerger);

        tabletxt=(TextView) findViewById(R.id.textView16);
        emptytxt=(TextView) findViewById(R.id.emptyTxt);
        scrollView=(ScrollView) findViewById(R.id.scrollView4);
        overallalltotal = (TextView) findViewById(R.id.totalTxt);

        scrollView.setVisibility(View.INVISIBLE);
//get tot from adpter
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciver,new IntentFilter("MyTotalAmmount"));


        cartArrayList = new ArrayList<>();


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference cartref = database.getReference("CartList").child("foodlist");


        cartref.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartArrayList.clear();

                if(!snapshot.exists())
                {
                    emptytxt.setVisibility(TextView.VISIBLE);
                }
                else {
                    scrollView.setVisibility(View.VISIBLE);
                }
                for(DataSnapshot snapshot1: snapshot.getChildren()) {


                    Cart cart = snapshot1.getValue(Cart.class);
                    cartArrayList.add(cart);


                }
                cartAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        cartAdapter = new cartviewAdapter(cartArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycleview.setLayoutManager(linearLayoutManager);
        recycleview.setAdapter(cartAdapter);




    }




    private void setTableno()
    {
        tabletxt.setText(String.valueOf(tableno));
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableno = tableno + 1;
                tabletxt.setText(String.valueOf(tableno));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tableno > 1) {
                    tableno = tableno - 1;
                }
                tabletxt.setText(String.valueOf(tableno));

            }
        });
        orderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeorder();


            }
        });
    }

    private void placeorder() {
        final DatabaseReference orderlistref = FirebaseDatabase.getInstance().getReference().child("orders");


        int randomoid = ThreadLocalRandom.current().nextInt(0, 1000);
        int tno = Integer.parseInt(tabletxt.getText().toString());
        final HashMap<String,Object> cartmap = new HashMap<>();
        cartmap.put("id",randomoid);
        cartmap.put("tableNo",tno);
        cartmap.put("served",false);
        cartmap.put("price",overallalltotal.getText().toString());



        orderlistref.child(String.valueOf("order"+randomoid)).updateChildren(cartmap).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {

                    Toast.makeText(cartlist.this,"Your order is placed.",Toast.LENGTH_SHORT).show();
                    Intent intent =new Intent(cartlist.this, MainActivity.class);
                    startActivity(intent);

                    DatabaseReference  reference = FirebaseDatabase.getInstance().getReference().child("CartList");
                    reference.child("foodlist").removeValue();

                }

            }
        });
        final HashMap<String,Object> itemmap = new HashMap<>();
        for (int i=0;i<cartArrayList.size();i++) {


            itemmap.put("id",cartArrayList.get(i).getFid());
            itemmap.put("image",cartArrayList.get(i).getImages());
            itemmap.put("name",cartArrayList.get(i).getFname());
            itemmap.put("qty",cartArrayList.get(i).getQty());
            //itemmap.put("totprice",overallalltotal.getText().toString());

            orderlistref.child(String.valueOf("order"+randomoid)).child("items").child(String.valueOf("item"+cartArrayList.get(i).getFid())).updateChildren(itemmap).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                    {

                    }
                }
            });
        }


    }
    private void calculatetotalprice() {

        int totalprice=0,toteach;
        for (int i=0;i<cartArrayList.size();i++) {

            int qty =Integer.valueOf(cartArrayList.get(i).getQty());
            int price =Integer.valueOf(cartArrayList.get(i).getPrice());
            toteach=qty*price;
            totalprice=totalprice+toteach;

        }
        overallalltotal.setText("LKR "+totalprice+".00");
    }

    private void initView() {


        plusBtn = findViewById(R.id.imageView5);
        minusBtn = findViewById(R.id.imageView4);
        tabletxt = findViewById(R.id.textView16);
        fidtxtincart=findViewById(R.id.fidtxtincart);
        scrollView = findViewById(R.id.scrollView4);
        txtnameofitem = findViewById(R.id.title2Txt);
        orderbtn=findViewById(R.id.placeorderbtn);
    }

    public BroadcastReceiver mMessageReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int totalbill =intent.getIntExtra("totalAmmount",0);

            overallalltotal.setText("Rs. " +totalbill+".00");
        }
    };

}