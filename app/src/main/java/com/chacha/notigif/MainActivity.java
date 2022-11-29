package com.chacha.notigif;

import static android.os.Build.VERSION.SDK_INT;
import static com.chacha.notigif.Files.createFiles;
import static com.chacha.notigif.Files.dir;
import static com.chacha.notigif.Files.readfile;
import static com.chacha.notigif.Utils.*;
import static com.chacha.notigif.Permissions.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.coniy.fileprefs.FileSharedPreferences;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.themes.GridType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    GridView gridView;
    String[] imagenames;
    ImageView marginLeftArrow, marginRightArrow;
    ImageButton deleteItem;
    View viewPrev;
    ConstraintLayout expandMarginsLayout;
    LinearLayout marginsLayout;
    SeekBar seekBar, seekBarRight, seekBarLeft, seekBarBottom, seekbarTop, seekbarTransparency;
    com.suke.widget.SwitchButton switchCarrier, switchDisable, switchFade, switchLock;
    TextView textView, textViewRight, textViewLeft, textViewBottom, textViewTop, textViewTransparency;
    Spinner horizontalSpinner, verticalSpinner, plansSpinner, gifNumberSpinner;
    int gifPosition=0, gifPosCount, selectedItemPos=0, gridViewSelectedItemPosition=-1;
    List<String> listGifPos;
    ArrayAdapter<String> arrayAdapter;

    @SuppressLint({"WorldReadableFiles", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = Preferences.loadPreferences(this);
        editor = Preferences.getEditor();

        checkXposed(this);
        showResourceHooksMsg();

        seekBar = findViewById(R.id.seekBar);
        switchCarrier = findViewById(R.id.switch1);
        switchDisable = findViewById(R.id.switchDisable);
        switchFade = findViewById(R.id.switchFade);
        switchLock = findViewById(R.id.switchLock);

        textView = findViewById(R.id.textView);
        expandMarginsLayout = findViewById(R.id.expand_margins_layout);
        marginsLayout = findViewById(R.id.margins_seekbars_layout);
        marginLeftArrow = findViewById(R.id.imageView);
        marginRightArrow = findViewById(R.id.imageView2);
        deleteItem = findViewById(R.id.deleteButton);

        seekBarRight = findViewById(R.id.seekBarRight);
        seekBarLeft = findViewById(R.id.seekBarLeft);
        seekBarBottom = findViewById(R.id.seekBarBottom);
        seekbarTop = findViewById(R.id.seekBarTop);
        seekbarTransparency = findViewById(R.id.seekBarTransparency);

        textViewRight = findViewById(R.id.textViewRight);
        textViewLeft = findViewById(R.id.textViewLeft);
        textViewBottom = findViewById(R.id.textViewBottom);
        textViewTop = findViewById(R.id.textViewTop);
        textViewTransparency = findViewById(R.id.textViewTransparency);

        horizontalSpinner = findViewById(R.id.spinner_horizontal);
        verticalSpinner = findViewById(R.id.spinner_vertical);
        plansSpinner = findViewById(R.id.spinner_plans);
        gifNumberSpinner = findViewById(R.id.spinner_gifnumber);

        listGifPos = new ArrayList<>(2);
        Permissions.checkPermission(this);
    }

    public void donate(View view) {
       Donation.openLink(this);
    }

    public void chooseGifClick(View view) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, 4).create();
        alertDialog.setMessage("What do you want to do ?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose from storage",
                (dialog, which) -> getImage());
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Choose from GIPHY",
                (dialog, which) -> giphy());
        alertDialog.setIcon(R.drawable.giphy);
        alertDialog.show();
    }

    public void giphy(){
        GPHSettings settings = new GPHSettings();
        GiphyDialogFragment gifsDialog = GiphyDialogFragment.Companion.newInstance(settings, "CVkzo6kRJQrLbK4sFDkOhAIzGZr6JZKC", false);
        settings.setTheme(GPHTheme.Dark);
        settings.setGridType(GridType.waterfall);
        settings.setSelectedContentType(GPHContentType.sticker);
        gifsDialog.show(this.getSupportFragmentManager(), "gifs_dialog");

        gifsDialog.setGifSelectionListener(new GiphyDialogFragment.GifSelectionListener() {
            @Override
            public void onGifSelected(@NotNull Media media, @Nullable String s, @NotNull GPHContentType gphContentType) {
                if (GifUtil.isWifiEnabled(getApplicationContext()) || !pref.getBoolean("showMobileDataWarning", true)) { //Not obliged to check if mobile data is on because if it's disable there is no gif
                    GifUtil.downloadGif(media.getId(), getApplicationContext(), MainActivity.this);
                } else {
                    GifUtil.showMobileDataWarning(media.getId(), getApplicationContext(), MainActivity.this);
                }
            }

                @Override
                public void onDismissed (@NotNull GPHContentType gphContentType){

                }

                @Override
                public void didSearchTerm (@NotNull String s){

                }
        });
    }

    public void killSystemUI(View view) {
        killSystemUi(this);
    }

    public void seekbarBtnSub0(View view) {
        seekbarButton(0, seekBar, textView, getResources().getString(R.string.size), "size");
    }

    public void seekbarBtnAdd0(View view) {
        seekbarButton(1, seekBar, textView, getResources().getString(R.string.size), "size");
    }

    public void seekbarBtnSub1(View view) {
        seekbarButton(0, seekbarTransparency, textViewTransparency, getResources().getString(R.string.opacity), "transparency");
    }

    public void seekbarBtnAdd1(View view) {
        seekbarButton(1, seekbarTransparency, textViewTransparency, getResources().getString(R.string.opacity), "transparency");
    }

    public void seekbarBtnSub2(View view) {
        seekbarButton(0, seekBarRight, textViewRight, getResources().getString(R.string.right_margin), "marginRight");
    }

    public void seekbarBtnAdd2(View view) {
        seekbarButton(1, seekBarRight, textViewRight, getResources().getString(R.string.right_margin), "marginRight");
    }

    public void seekbarBtnSub3(View view) {
        seekbarButton(0, seekBarLeft, textViewLeft, getResources().getString(R.string.left_margin), "marginLeft");
    }

    public void seekbarBtnAdd3(View view) {
        seekbarButton(1, seekBarLeft, textViewLeft, getResources().getString(R.string.left_margin), "marginLeft");
    }

    public void seekbarBtnSub4(View view) {
        seekbarButton(0, seekBarBottom, textViewBottom, getResources().getString(R.string.bottom_margin), "marginBottom");
    }

    public void seekbarBtnAdd4(View view) {
        seekbarButton(1, seekBarBottom, textViewBottom, getResources().getString(R.string.bottom_margin), "marginBottom");
    }

    public void seekbarBtnSub5(View view) {
        seekbarButton(0, seekbarTop, textViewTop, getResources().getString(R.string.top_margin), "marginTop");
    }

    public void seekbarBtnAdd5(View view) {
        seekbarButton(1, seekbarTop, textViewTop, getResources().getString(R.string.top_margin), "marginTop");
    }
    public class ImagesAdapter extends BaseAdapter {
        private final Context mContext;

        public ImagesAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            try {
                return imagenames.length;
            } catch (NullPointerException e) {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public ImageView getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);
            if(imagenames[position].endsWith(".gif")) {
                Glide.with(mContext)
                        .asGif()
                        .load(imagenames[position])
                        .override(Target.SIZE_ORIGINAL)
                        .thumbnail(/*sizeMultiplier=*/ 0.25f)
                        .into(imageView);
            } else {
                Glide.with(mContext)
                        .asBitmap()
                        .load(imagenames[position])
                        .override(Target.SIZE_ORIGINAL)
                        .thumbnail(/*sizeMultiplier=*/ 0.25f)
                        .into(imageView);
            }
            int displayMetrics = getResources().getDisplayMetrics().densityDpi;
            if (displayMetrics >= 200 && displayMetrics <= 400) {//xhdpi
                imageView.setLayoutParams(new GridView.LayoutParams(170, 170));
            } else if (displayMetrics > 400 && displayMetrics <= 600) {//xxhdpi
                imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
            } else if (displayMetrics > 600 && displayMetrics <= 800) {//xxxhdpi
                imageView.setLayoutParams(new GridView.LayoutParams(330, 330));
            } else if (displayMetrics > 800) {
                imageView.setLayoutParams(new GridView.LayoutParams(410, 410));
            }

            if(position==gridViewSelectedItemPosition){
                imageView.setBackgroundResource(R.drawable.shape_selected);
                viewPrev = imageView;
            } else {
                imageView.setBackgroundResource(R.drawable.shape);
            }

            return imageView;
        }
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public void startProgram() {
        try {
            imagenames = new String[Objects.requireNonNull(dir.listFiles()).length];
        } catch (NullPointerException ignored) { }
        readfile(this);
        Donation.remindDonation(this);

        switchCarrier.setChecked(pref.getBoolean("carrierLabelBoolean", false));
        switchDisable.setChecked(pref.getBoolean("disableBoolean"+gifPosition, false));
        switchFade.setChecked(pref.getBoolean("fade", true));
        switchLock.setChecked(pref.getBoolean("showLock", true));

        seekbarTransparency.setProgress(pref.getInt("transparency"+gifPosition, 100));
        seekBar.setProgress(pref.getInt("size"+gifPosition, 390));
        seekBarRight.setProgress(pref.getInt("marginRight"+gifPosition, 0));
        seekBarLeft.setProgress(pref.getInt("marginLeft"+gifPosition, 0));
        seekBarBottom.setProgress(pref.getInt("marginBottom"+gifPosition, 0));
        seekbarTop.setProgress(pref.getInt("marginTop"+gifPosition, 0));

        textViewRight.setText(getResources().getString(R.string.right_margin) + " " + seekBarRight.getProgress());
        textViewLeft.setText(getResources().getString(R.string.left_margin) + " " + seekBarLeft.getProgress());
        textViewBottom.setText(getResources().getString(R.string.bottom_margin) + " " + seekBarBottom.getProgress());
        textViewTop.setText(getResources().getString(R.string.top_margin) + " " + seekbarTop.getProgress());
        textView.setText(getResources().getString(R.string.size) + " " + seekBar.getProgress());
        textViewTransparency.setText(getResources().getString(R.string.opacity) + " " + seekbarTransparency.getProgress());

        horizontalSpinner.setSelection(pref.getInt("horizontalSpinnerPosition"+gifPosition, 2));
        verticalSpinner.setSelection(pref.getInt("verticalSpinnerPosition"+gifPosition, 1));
        plansSpinner.setSelection(pref.getInt("plansSpinnerPosition"+gifPosition, 0));

        gifPosCount = pref.getInt("gifPosCount", 1);
        for(int i=0; i<gifPosCount; i++){
            listGifPos.add("GIF : " + (i+1));
        }
        listGifPos.add("+");
        arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listGifPos);
        ImagesAdapter imagesAdapter = new ImagesAdapter(this);
        gifNumberSpinner.setAdapter(arrayAdapter);
        gridView = findViewById(R.id.gridView);
        gridViewSelectedItemPosition=pref.getInt("ChoosenImagePos"+gifPosition, -1);
        gridView.setAdapter(imagesAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if(viewPrev!=null) {
                viewPrev.setBackgroundResource(R.drawable.shape);
            }
            view.setBackgroundResource(R.drawable.shape_selected);
            viewPrev = view;
            gridViewSelectedItemPosition=position;
            editor.putString("ChoosenImagePath"+gifPosition, imagenames[position]);
            editor.putInt("ChoosenImagePos"+gifPosition, position);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        });

        gridView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, 4).create();
            alertDialog.setTitle("Wait");
            alertDialog.setMessage("Are you sure to delete it ?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                    (dialog, which) -> {
                        File fileToRemove = new File(imagenames[i]); // path only
                        try {
                            FileUtils.forceDelete(fileToRemove);
                            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
                            refreshGridView();
                        } catch (IOException e) {
                            Permissions.checkPermission(this);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                    (dialog, which) -> alertDialog.cancel());
            alertDialog.show();
            return false;
        });


        deleteItem.setOnClickListener(view -> {
            int pos = gifNumberSpinner.getSelectedItemPosition();
            if (pos != gifPosCount) { //Cannot delete the "+" button and minimum 1 page
                final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, 4).create();
                if(gifPosCount > 1) {
                    alertDialog.setMessage("Are you sure to delete this page (" + (selectedItemPos + 1) + ") ?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            (dialog, which) -> {
                                editor.putInt("gifPosCount", --gifPosCount);
                                editor.remove("size" + pos);
                                editor.remove("transparency" + pos);
                                editor.remove("marginRight" + pos);
                                editor.remove("marginLeft" + pos);
                                editor.remove("marginBottom" + pos);
                                editor.remove("marginTop" + pos);
                                editor.remove("horizontalSpinnerPosition" + pos);
                                editor.remove("verticalSpinnerPosition" + pos);
                                editor.remove("plansSpinnerPosition" + pos);
                                editor.remove("disableBoolean" + pos);
                                editor.remove("ChoosenImagePath" + pos);
                                editor.remove("ChoosenImagePos" + pos);
                                editor.commit();
                                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
                                listGifPos.remove(pos);

                                for(int i=pos; i<gifPosCount; i++){ //Shift all saved values to avoid problems to load them (shift only values superior as the removed position)
                                    editor.putInt("size"+i, pref.getInt("size"+(i+1), 390));
                                    editor.putInt("transparency"+i, pref.getInt("transparency"+(i+1), 100));
                                    editor.putInt("marginRight"+i, pref.getInt("marginRight"+(i+1), 0));
                                    editor.putInt("marginLeft"+i, pref.getInt("marginLeft"+(i+1), 0));
                                    editor.putInt("marginBottom"+i, pref.getInt("marginBottom"+(i+1), 0));
                                    editor.putInt("marginTop"+i, pref.getInt("marginTop"+(i+1), 0));
                                    editor.putInt("horizontalSpinnerPosition"+i, pref.getInt("horizontalSpinnerPosition"+(i+1), 2));
                                    editor.putInt("verticalSpinnerPosition"+i, pref.getInt("verticalSpinnerPosition"+(i+1), 1));
                                    editor.putInt("plansSpinnerPosition"+i, pref.getInt("plansSpinnerPosition"+(i+1), 0));
                                    editor.putBoolean("disableBoolean"+i, pref.getBoolean("disableBoolean"+(i+1), false));
                                    editor.putString("ChoosenImagePath"+i, pref.getString("ChoosenImagePath"+(i+1), ""));
                                    editor.putInt("ChoosenImagePos"+i, pref.getInt("ChoosenImagePos"+(i+1), -1));
                                    editor.commit();
                                    FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
                                }
                                for(int i=0; i<gifPosCount; i++){ //Refresh the spinner
                                    listGifPos.set(i, "GIF : " + (i+1));
                                }
                                gifNumberSpinner.setAdapter(arrayAdapter);
                                gifNumberSpinner.setSelection(pos - 1);
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                            (dialog, which) -> {
                            });
                } else {
                    alertDialog.setMessage("You cannot delete all pages");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                            (dialog, which) -> {});
                }
                alertDialog.show();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                editor.putInt("size"+gifPosition, progress);
                textView.setText(getResources().getString(R.string.size) + " " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }
        });
        seekbarTransparency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                editor.putInt("transparency"+gifPosition, progress);
                textViewTransparency.setText(getResources().getString(R.string.opacity) + " " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }
        });

        switchCarrier.setOnCheckedChangeListener((view, isChecked) -> {
            editor.putBoolean("carrierLabelBoolean", isChecked);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        });

        switchDisable.setOnCheckedChangeListener((view, isChecked) -> {
            editor.putBoolean("disableBoolean"+gifPosition, isChecked);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        });

        switchFade.setOnCheckedChangeListener((view, isChecked) -> {
            editor.putBoolean("fade", isChecked);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        });

        switchLock.setOnCheckedChangeListener((view, isChecked) -> {
            editor.putBoolean("showLock", isChecked);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
        });

        expandMarginsLayout.setOnClickListener(v -> {
            Animation animation;
            if (marginsLayout.getVisibility()==View.GONE) {
                marginsLayout.setVisibility(View.VISIBLE);
                animation = AnimationUtils.loadAnimation(this, R.anim.bounce);
                marginsLayout.setAnimation(animation);
                marginsLayout.animate();
                marginLeftArrow.setRotation(180);
                marginRightArrow.setRotation(180);
            } else {
                marginsLayout.setVisibility(View.GONE);
                animation = AnimationUtils.loadAnimation(this, R.anim.bounce_end);
                marginsLayout.setAnimation(animation);
                marginsLayout.animate();
                marginLeftArrow.setRotation(360);
                marginRightArrow.setRotation(360);
            }
        });

        seekBarRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("marginRight"+gifPosition, progress);
                textViewRight.setText(getResources().getString(R.string.right_margin) + " " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("marginLeft"+gifPosition, progress);
                textViewLeft.setText(getResources().getString(R.string.left_margin) +" " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("marginBottom"+gifPosition, progress);
                textViewBottom.setText(getResources().getString(R.string.bottom_margin) + " " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editor.putInt("marginTop"+gifPosition, progress);
                textViewTop.setText(getResources().getString(R.string.top_margin) + " " + progress);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

       horizontalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               editor.putInt("horizontalSpinnerPosition"+gifPosition, position);
               editor.commit();
               FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
           }
           @Override
           public void onNothingSelected(AdapterView<?> parent) {}
       });

        verticalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("verticalSpinnerPosition"+gifPosition, position);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        plansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("plansSpinnerPosition"+gifPosition, position);
                editor.commit();
                FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        gifNumberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==gifPosCount){ //Add a gif page (+)
                    editor.putInt("gifPosCount", ++gifPosCount);
                    editor.commit();
                    FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
                    listGifPos.set(position, "GIF : " + (position+1));
                    listGifPos.add("+");
                    gifNumberSpinner.setAdapter(arrayAdapter);
                    gifNumberSpinner.setSelection(position);
                } else {
                    selectedItemPos=position;
                    gifPosition=position;
                    seekBar.setProgress(pref.getInt("size"+gifPosition, 390));
                    seekbarTransparency.setProgress(pref.getInt("transparency"+gifPosition, 100));
                    seekBarRight.setProgress(pref.getInt("marginRight"+gifPosition, 0));
                    seekBarLeft.setProgress(pref.getInt("marginLeft"+gifPosition, 0));
                    seekBarBottom.setProgress(pref.getInt("marginBottom"+gifPosition, 0));
                    seekbarTop.setProgress(pref.getInt("marginTop"+gifPosition, 0));

                    horizontalSpinner.setSelection(pref.getInt("horizontalSpinnerPosition"+gifPosition, 2));
                    verticalSpinner.setSelection(pref.getInt("verticalSpinnerPosition"+gifPosition, 1));
                    plansSpinner.setSelection(pref.getInt("plansSpinnerPosition"+gifPosition, 0));

                    switchDisable.setChecked(pref.getBoolean("disableBoolean"+gifPosition, false));

                    refreshGridView();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        textView.setOnLongClickListener(view -> {
            editor.putInt("size", 390);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
            seekBar.setProgress(pref.getInt("size", 390));
            return false;
        });
    }

    public void getImage() {
        // To open up a gallery browser
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image"), 1);
        // To handle when an image is selected from the browser, add the following to your Activity
    }

    public void refreshGridView() {
        try {
            imagenames = new String[Objects.requireNonNull(dir.listFiles()).length];
            readfile(this);
            ImagesAdapter imagesAdapter = new ImagesAdapter(this);
            gridView.setAdapter(imagesAdapter);
            imagesAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Permissions.checkPermission(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                Context ctx = getApplicationContext();
                File srcFile;
                try {
                    srcFile = new File(Objects.requireNonNull(RealPathClass.getRealPathFromURI(ctx, data.getData())));  // path + filename
                    try {
                        FileUtils.copyFileToDirectory(srcFile, dir);
                        refreshGridView();
                    } catch (IOException e) {
                        Toast.makeText(ctx, "CAN NOT COPY THE FILE, CHECK THE APP PERMISSIONS !", Toast.LENGTH_LONG).show();
                        requestPermission(this);
                        e.printStackTrace();
                    }
                } catch (Exception ignored) {
                    // Checkpermission();
                    //Toast.makeText(ctx, "An error occured !", Toast.LENGTH_LONG).show();
                }
                break;
            case 2296:
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // perform action when allow permission success
                        createFiles(this);
                    } else {
                        showPermissionError();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createFiles(this);
                requestPermission(this);
                // permission was granted
            } else {
                showPermissionError();
            }
        }
    }

    public void showPermissionError(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this, 4).create();
        alertDialog.setTitle("Heyyy where are you going to go like this ?");
        alertDialog.setCancelable(true);
        alertDialog.setMessage("Storage permission was not granted. This permission is necessary to store all gifs on the device.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ask again",
                (dialog, which) -> requestPermission(this));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Go to settings",
                (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + MY_PACKAGE_NAME));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                });
        alertDialog.show();
    }

    public void showResourceHooksMsg(){
        if (!pref.getBoolean("showResourceHooks", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, 4);
            builder.setMessage("You need to enable resource hooks in EdXposed Manager or LSPosed otherwise the module will not work !")
                    .setNegativeButton("Okay", (dialog, id) -> {})
                    .setPositiveButton("NEVER SHOW THIS PLEAAASEE", (dialog, id) -> {
                        editor.putBoolean("showResourceHooks", true);
                        editor.apply();
                        FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @SuppressLint("SetTextI18n")
    public void seekbarButton(int mode, SeekBar seekBar, TextView textView, String text, String pref){
        int progress = seekBar.getProgress();
            switch (mode) {
                case 0:
                    if(progress>0)
                        progress--;
                    break;
                case 1:
                    if(progress<seekBar.getMax())
                        progress++;
                    break;
            }
            seekBar.setProgress(progress);
            textView.setText(text + " " + progress);
            editor.putInt(pref, progress);
            editor.commit();
            FileSharedPreferences.makeWorldReadable(MY_PACKAGE_NAME, Utils.PREFERENCES_FILE_NAME);
    }
}