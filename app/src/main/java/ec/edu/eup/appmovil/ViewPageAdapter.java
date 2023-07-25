package ec.edu.eup.appmovil;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class ViewPageAdapter extends PagerAdapter {
    Context context;
    ArrayList<String> images=new ArrayList<>();
    LayoutInflater mLayoutInflater;

    private VideoView videoView;
    private ImageView control_button;
    private int play_or_not=0;

public ViewPageAdapter(Context context, ArrayList images){
this.context=context;
this.images=images;
mLayoutInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
}

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==((FrameLayout) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
      container.removeView((FrameLayout) object);

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        Uri imgUri= Uri.parse("file://"+images.get(position));
        Log.w("ViewPageAdapter","Out: "+imgUri.getPath());
        String[] arrStr=images.get(position).split(".j",2);
        if(arrStr.length==2){
           View itemView = mLayoutInflater.inflate(R.layout.item,container,false);
           ImageView image = (ImageView) itemView.findViewById(R.id.image);
           image.setImageURI(imgUri);
           Objects.requireNonNull(container).addView(itemView);
           return itemView;
       }else{
            View vide_item_view = mLayoutInflater.inflate(R.layout.video_item,container,false);
            videoView=vide_item_view.findViewById(R.id.videoview);
            control_button=vide_item_view.findViewById(R.id.control_button);
            MediaController mediaController=new MediaController(this.context);
            mediaController.setAnchorView(videoView);

            videoView.setVideoURI(imgUri);
            videoView.requestFocus();

            control_button.setImageResource(R.drawable.play_button);
            videoView.start();
            play_or_not=1;

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    control_button.setImageResource(R.drawable.replay_button);
                    play_or_not=0;
                }
            });

                control_button.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                       if(event.getAction()==MotionEvent.ACTION_DOWN){
                           control_button.setColorFilter(Color.DKGRAY);
                           return  true;
                       }
                       if(event.getAction()==MotionEvent.ACTION_UP){
                           control_button.setColorFilter(Color.WHITE);
                           if(play_or_not==0){
                               control_button.setImageResource(R.drawable.pause_button);
                               play_or_not=1;
                               videoView.start();
                           }
                           else{
                               control_button.setImageResource(R.drawable.play_button);
                               play_or_not=0;
                               videoView.pause();
                           }
                       }
                        return false;
                    }
                });
                Objects.requireNonNull(container).addView(vide_item_view);
                return  vide_item_view;
        }

    }
}
