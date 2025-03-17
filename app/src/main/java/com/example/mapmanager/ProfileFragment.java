package com.example.mapmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private TextView userTextView, emailTextView, dateTextView, quitAccountText, deleteAccountText, changePassText;
    private View view1, view2, view3, view6, view7, view8, view9;
    private ImageView deleteAccountView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userTextView = view.findViewById(R.id.userTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        deleteAccountView = view.findViewById(R.id.deleteAccountView);
        quitAccountText = view.findViewById(R.id.quitAccountText);
        deleteAccountText = view.findViewById(R.id.deleteAccountText);
        changePassText = view.findViewById(R.id.changePassText);
        view1 = view.findViewById(R.id.view);
        view2 = view.findViewById(R.id.view2);
        view3 = view.findViewById(R.id.view3);
        view6 = view.findViewById(R.id.view6);
        view7 = view.findViewById(R.id.view7);
        view8 = view.findViewById(R.id.view8);
        view9 = view.findViewById(R.id.view9);
        view1.post(new Runnable() {
            @Override
            public void run() {
                view2.bringToFront();
                view3.bringToFront();
            }
        });
        view6.post(new Runnable() {
            @Override
            public void run() {
                view8.bringToFront();
            }
        });
        view7.post(new Runnable() {
            @Override
            public void run() {
                view9.bringToFront();
                deleteAccountView.bringToFront();
            }
        });
        return view;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userData = databaseReference.child("users").child(user.getUid());
            userData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("userName").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String dateBirthday = dataSnapshot.child("dateBirthday").getValue(String.class);
                        if (userName != null) {
                            userTextView.setText(userName);
                        } else {
                            userTextView.setText("User name not found");
                        }
                        if (email != null) {
                            emailTextView.setText(email);
                        } else {
                            emailTextView.setText("Email not found");
                        }
                        if (dateBirthday != null) {
                            dateTextView.setText(dateBirthday);
                        } else {
                            dateTextView.setText("Date birthday not found");
                        }
                    } else {
                        userTextView.setText("User data not found");
                        emailTextView.setText("User data not found");
                        dateTextView.setText("User data not found");
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    userTextView.setText("Error loading data");
                }
            });
        } else {
            userTextView.setText("User not logged in");
        }
        quitAccountText.setOnClickListener(v -> {
            signOut();
        });
        deleteAccountText.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.account_delete_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
//            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            View deleteView = dialogView.findViewById(R.id.accountDeleteView);
            View cancelView = dialogView.findViewById(R.id.cancelView);
            deleteView.setOnClickListener(v1 -> {
                deleteAccount();
                dialog.dismiss();
            });

            cancelView.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();

        });
        changePassText.setOnClickListener(v ->{
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_pass_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogView);
//            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            View changeView = dialogView.findViewById(R.id.applyChangePassView);
            View cancelView = dialogView.findViewById(R.id.cancelView);
            EditText editPasswordText, confirmPasswordText;
            editPasswordText = dialogView.findViewById(R.id.editPasswordText);
            confirmPasswordText = dialogView.findViewById(R.id.confirmPasswordText);

            changeView.setOnClickListener(v1 -> {
                String password = editPasswordText.getText().toString();
                String confirmPassword = confirmPasswordText.getText().toString();
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireActivity(), "@string/fill_all_fields_text", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.equals(confirmPassword)) {
                    changeUserPass(password);
                } else {
                    Toast.makeText(requireActivity(), "@string/different_passwords_text", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
            });

            cancelView.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            dialog.show();
        });

    }
    void changeUserPass(String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();
        user.updatePassword(newPass)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(requireContext(), "Pass change", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Pass don't change", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference userData = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
                            userData.removeValue();
                            Toast.makeText(requireActivity(), R.string.enter_success_text, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireActivity(), LoginActivity.class);
                            intent.putExtra("CLEAR_DATA", true);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), "Account deletion failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Account deletion failed: " + e.getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
    void signOut() {
        mAuth.signOut();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(requireActivity(), R.string.enter_success_text, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.putExtra("CLEAR_DATA", true);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        });
    }

}