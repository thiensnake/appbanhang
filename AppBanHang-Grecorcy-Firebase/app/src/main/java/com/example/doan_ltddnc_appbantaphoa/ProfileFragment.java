package com.example.doan_ltddnc_appbantaphoa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doan_ltddnc_appbantaphoa.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    EditText edtEmail ,edtFirstname ,edtLastname ,edtPhone ;
    Button btnUpdate ,btnLogout ;
    CircleImageView imgProfile ;
    FirebaseAuth auth ;
    FirebaseStorage firebaseStorage ;
    FirebaseDatabase database ;
    LoadingDialog loadingDialog;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtEmail =view.findViewById(R.id.edtEmail);
        edtFirstname =view.findViewById(R.id.edtFirstname);
        edtLastname =view.findViewById(R.id.edtLastname);
        edtPhone=view.findViewById(R.id.edtPhone);
        imgProfile =view.findViewById(R.id.img_profile);
        btnUpdate=view.findViewById(R.id.btnUpdate);
        btnLogout =view.findViewById(R.id.btnLogout);
        auth =FirebaseAuth.getInstance();
        database =FirebaseDatabase.getInstance();
        firebaseStorage =FirebaseStorage.getInstance();
        loadingDialog =new LoadingDialog(getContext());

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user =snapshot.getValue(User.class);
                                Glide.with(getContext()).load(user.getProfileImg()).into(imgProfile);
                                edtEmail.setText(user.getEmail());
                                edtFirstname.setText(user.getFirstname());
                                edtLastname.setText(user.getLastname());
                                edtPhone.setText(user.getMobile());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,33);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }


        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(getActivity() ,LoginActivity.class));
            }
        });


    }
    private void updateUserProfile() {

        loadingDialog.ShowDialog("Cập nhật");
        Map<String ,Object> mapUser =new HashMap<>() ;
        mapUser.put("email",edtEmail.getText().toString());
        mapUser.put("firstname",edtFirstname.getText().toString());
        mapUser.put("lastname",edtLastname.getText().toString());
        mapUser.put("mobile",edtPhone.getText().toString());
        database.getReference().child("Users").child(auth.getCurrentUser().getUid()).updateChildren(mapUser).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingDialog.HideDialog();
                        Toast.makeText(getContext(), "OKK", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData()!=null){
            Uri profileUri =data.getData();
            imgProfile.setImageURI(profileUri);

            final StorageReference reference = firebaseStorage.getReference().child("profile_picture")
                    .child(FirebaseAuth.getInstance().getUid());

            reference.putFile(profileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profileImg").setValue(uri.toString());

                        }
                    });
                }
            });
        }
    }
}