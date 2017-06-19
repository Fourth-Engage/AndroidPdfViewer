package xyz.guutong.androidpdfviewer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.FileNotFoundException;

import xyz.guutong.androidpdfviewer.Utils.DownloadFile;

public class PdfViewActivity extends AppCompatActivity implements DownloadFile.Listener, OnPageChangeListener, OnLoadCompleteListener {

    public static final String EXTRA_PDF_URL = "EXTRA_PDF_URL";
    public static final String EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE";
    public static final String EXTRA_SHOW_SCROLL = "EXTRA_SHOW_SCROLL";
    public static final String EXTRA_SWIPE_HORIZONTAL = "EXTRA_SWIPE_HORIZONTAL";
    public static final String EXTRA_SHOW_SHARE_BUTTON = "EXTRA_SHOW_SHARE_BUTTON";
    public static final String EXTRA_SHOW_CLOSE_BUTTON = "EXTRA_SHOW_CLOSE_BUTTON";
    public static final String EXTRA_TOOLBAR_COLOR = "EXTRA_TOOLBAR_COLOR";

    private static final int MENU_CLOSE = Menu.FIRST;
    private static final int MENU_SHARE = Menu.FIRST + 1;

    private Toolbar toolbar;
    private com.github.barteksc.pdfviewer.PDFView pdfView;
    private Intent intentUrl;
    private ProgressBar progressBar;
    private String pdfUrl;
    private Boolean showScroll;
    private Boolean swipeHorizontal;
    private String toolbarColor = "#1191d5";
    private String toolbarTitle;
    private Boolean showShareButton;
    private Boolean showCloseButton;

    private DefaultScrollHandle scrollHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        intentUrl = getIntent();
        pdfUrl = intentUrl.getStringExtra(EXTRA_PDF_URL);
        toolbarTitle = intentUrl.getStringExtra(EXTRA_PDF_TITLE) == null ? "" : intentUrl.getStringExtra(EXTRA_PDF_TITLE);
        toolbarColor = intentUrl.getStringExtra(EXTRA_TOOLBAR_COLOR) == null ? toolbarColor : intentUrl.getStringExtra(EXTRA_TOOLBAR_COLOR);
        showScroll = intentUrl.getBooleanExtra(EXTRA_SHOW_SCROLL, false);
        swipeHorizontal = intentUrl.getBooleanExtra(EXTRA_SWIPE_HORIZONTAL, false);
        showShareButton = intentUrl.getBooleanExtra(EXTRA_SHOW_SHARE_BUTTON, true);
        showCloseButton = intentUrl.getBooleanExtra(EXTRA_SHOW_CLOSE_BUTTON, true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        pdfView = (com.github.barteksc.pdfviewer.PDFView) findViewById(R.id.pdfView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /* set color colorPrimaryDark*/
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(toolbarColor), hsv);
        hsv[2] *= 0.8f;
        int colorPrimaryDark = Color.HSVToColor(hsv);
        if (Build.VERSION.SDK_INT >= 21) {
            this.getWindow().setStatusBarColor(colorPrimaryDark);
        }

        toolbar.setBackgroundColor(Color.parseColor(toolbarColor));
        toolbar.setTitle(toolbarTitle);

        if (showScroll) {
            scrollHandle = new DefaultScrollHandle(this);
        }

        setSupportActionBar(toolbar);

        progressBar.setVisibility(View.VISIBLE);


        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else {
            getMyPdf();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getMyPdf();
                }
                return;
            }
        }
    }

    private void getMyPdf() {
        // Log.d("pdfUrl", pdfUrl);
        // loading local pdf file
        try {
            File file = new File(pdfUrl);

            if (file.exists()) {
                pdfView.fromFile(file)
                        .defaultPage(0)
                        .onPageChange(this)
                        .enableAnnotationRendering(true)
                        .onLoad(this)
                        .scrollHandle(scrollHandle)
                        .swipeHorizontal(swipeHorizontal)
                        .load();
            } else {
                throw new FileNotFoundException("");
            }

        } catch (FileNotFoundException error) {
            progressBar.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Could not open file.");
            builder.show();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        // TODO: show a share button, allow user to save/rename file to downloads, remove cache
        // if (showShareButton)
        //     menu.add(0, MENU_SHARE, Menu.NONE, R.string.share)
        //             .setIcon(R.drawable.ic_share)
        //             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // if (showCloseButton)
        menu.add(0, 1, MENU_CLOSE, R.string.close)
                .setIcon(R.drawable.ic_close)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == MENU_CLOSE) {
            finish();
        }
//        else if (itemId == MENU_SHARE) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            CharSequence[] itemsAlert = {"Copy link", "Open browser"};
//
//            builder.setItems(itemsAlert, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int itemIndex) {
//                    final int COPY_LINK = 0;
//                    final String label = "URL";
//
//                    if (itemIndex == COPY_LINK) {
//                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                        ClipData clip = ClipData.newPlainText(label, pdfUrl);
//                        clipboard.setPrimaryClip(clip);
//                        return;
//                    }
//
//                    Intent intentBrowser = new Intent(Intent.ACTION_VIEW);
//                    intentBrowser.setData(Uri.parse(pdfUrl));
//                    startActivity(intentBrowser);
//                }
//            });
//            builder.show();
//
//        }
        return true;
    }

    @Override
    public void onSuccess(String url, String destinationPath) {
    }

    @Override
    public void onFailure(Exception e) {
    }

    @Override
    public void onProgressUpdate(int progress, int total) {
    }

    @Override
    public void loadComplete(int nbPages) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
    }
}
