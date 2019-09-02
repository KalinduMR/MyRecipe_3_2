package com.wiseowl.myrecipe.adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.storage.StorageManager;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.wiseowl.myrecipe.R;
import com.wiseowl.myrecipe.models.ModelPost;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.zip.DataFormatException;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder> {

    Context context;
    List<ModelPost> postList;
    String userid;

    public AdapterPost(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int i) {

        final String uid =postList.get(i).getUid();
        String uEmail =postList.get(i).getuEmail();
        String uName =postList.get(i).getuName();
        String uDp =postList.get(i).getuDp();
        final String pId =postList.get(i).getpId();
        String pTitle =postList.get(i).getpTitle();
        String pDescription =postList.get(i).getpDes();
        final String pImage =postList.get(i).getpImage();
        String pTimeStamp =postList.get(i).getpTime();


        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/mm/yy hh.mm aa", calendar).toString();

        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);

        try{

            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        if (pImage.equals("noImage")){
            holder.pImageIv.setVisibility(View.GONE);

        }else{

            holder.pImageIv.setVisibility(View.VISIBLE);
            try{

                Picasso.get().load(pImage).into(holder.pImageIv);
            }
            catch (Exception e){

            }


        }

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showmoreop(holder.moreBtn,uid,userid,pId,pImage);
                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Star", Toast.LENGTH_SHORT).show();
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showmoreop(ImageButton moreBtn, String uid, String userid, final String pId, final String pImage) {

        PopupMenu popupMenu=new PopupMenu(context,moreBtn, Gravity.END);
        if (uid.equals(userid))
        {
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0)
                {
                    Delete(pId,pImage);
                }
                    return false;
            }

        });
        popupMenu.show();
    }

    private void Delete(String pId, String pImage) {
        if (pImage.equals("noImage"))
        {
            delewithoutimg(pId);
        }
        else {
            delewithimg(pId,pImage);
        }
    }

    private void delewithimg(final String pId, String pImage) {

        final ProgressDialog prodiag=new ProgressDialog(context);
        prodiag.setMessage("Deleting in Progress");


        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fqry= (Query) FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fqry.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        prodiag.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                prodiag.dismiss();

            }
        });
    }

    private void delewithoutimg(String pId) {
        final ProgressDialog prodiag=new ProgressDialog(context);
        prodiag.setMessage("Deleting in Progress");

        Query fqry= (Query) FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fqry.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                prodiag.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {


        ImageView uPictureIv, pImageIv;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;


        public MyHolder(@NonNull View itemView) {
            super(itemView);


            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
        }
    }
}