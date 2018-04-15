
package com.apps.meet.userauthentication;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<ContactInfo> mContactList;
    private DisplayContactsActivity mContext;

    public ContactAdapter(List<ContactInfo> contactList, final DisplayContactsActivity context) {
        this.mContactList = contactList;
        this.mContext = context;

    }


    @Override
    public int getItemCount() {
        return mContactList.size();
    }

    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        ContactInfo ci = mContactList.get(i);
        contactViewHolder.vName.setText(ci.name);
            //Show circular image with first character of name
            contactViewHolder.vProfileImage.setVisibility(View.VISIBLE);
            contactViewHolder.vCircleProfileImage.setVisibility(View.INVISIBLE);

            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            int color = generator.getColor(ci.name + ci.name.charAt(0));
            Log.d("color is " , Integer.toString(color));

            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(Character.toString(ci.name.charAt(0)), color);

            contactViewHolder.vProfileImage.setImageDrawable(drawable);

    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_layout, viewGroup, false);

            return new ContactViewHolder(itemView);
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        protected TextView vName;
        protected ImageView vProfileImage;
        protected ImageView vCircleProfileImage;

        public ContactViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.txtName);
            vProfileImage = (ImageView)  v.findViewById(R.id.profile_image);
            vCircleProfileImage = (ImageView)  v.findViewById(R.id.profile_image_circle);
            v.setOnLongClickListener(this);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();
                    mContext.onContactClick(pos);

                }
            });
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("Adapter", "long click called");

            return true;
        }
    }
}