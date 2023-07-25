package ec.edu.eup.appmovil;


import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    ViewPager mViewPager;
    ArrayList<String> filePath= new ArrayList<>();
    ViewPageAdapter viewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/ImagePro");
        createFile(folder);
        mViewPager=(ViewPager)findViewById(R.id.viewPagerMain);
        viewPageAdapter=new ViewPageAdapter(GalleryActivity.this,filePath);
        mViewPager.setAdapter(viewPageAdapter);
    }

    private void createFile(File folder) {
        File listFile []= folder.listFiles();

        if(listFile != null){
            for(int i=0;i<listFile.length;i++){
                filePath.add(listFile[i].getAbsolutePath());
            }
        }

    }
}