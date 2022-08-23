package com.example.cliff_screen.Adapter;


import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.cliff_screen.Email.JavaMailApi;
import com.example.cliff_screen.MainActivity;
import com.example.cliff_screen.Model.User;
import com.example.cliff_screen.ProfileActivity;
import com.example.cliff_screen.R;
import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_display_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = userList.get(position);

        holder.type.setText(user.getType());
        if (user.getType().equals("Charity")) {
            holder.emailNow.setVisibility(View.VISIBLE);
        }
        holder.userEmail.setText(user.getEmail());
        holder.phoneNumber.setText(user.getPhoneNumber());
        holder.userName.setText(user.getName());

        Glide.with(context).load(user.getProfile_picture_url()).into(holder.userProfileImage);

        final String nameOfTheReceiver= user.getName();
        final  String idOfTheReceiver= user.getId();
        //sending email

        holder.emailNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("SEND EMAIL")
                        .setMessage("Send email to " + user.getName() + "?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference()
                                        .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String nameOfSender= snapshot.child("name").getValue().toString();
                                        String email= snapshot.child("email").getValue().toString();
                                        String phone= snapshot.child("phoneNumber").getValue().toString();

                                        String mEmail= user.getEmail();
                                        String mSubject= "CHARITY DONATION";
                                        String mMessage= "Hello" + nameOfTheReceiver +", "+ nameOfSender+
                                                "would like to enquire about your organization. Here are their details:\n"
                                                +"Name: " +nameOfSender+ "\n"
                                                +"Phone Number: "+phone+ "\n"
                                                +"Email: " +email+ "\n"
                                                +"Reach out to them, Thank you!\n"
                                                +"M-Chango, Saidia Wananchi wenzako!";

                                        JavaMailApi javaMailApi=new JavaMailApi(context, mEmail, mSubject, mMessage);
                                        javaMailApi.execute();

                                        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("emails")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        senderRef.child(idOfTheReceiver).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    DatabaseReference receiverRef= FirebaseDatabase.getInstance().getReference("emails")
                                                            .child(idOfTheReceiver);
                                                    receiverRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView userProfileImage;
        public TextView type, userName, userEmail, phoneNumber;
        public Button emailNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            type = itemView.findViewById(R.id.type);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            phoneNumber = itemView.findViewById(R.id.phoneNumber);
            emailNow = itemView.findViewById(R.id.emailNow);
        }
    }
}
