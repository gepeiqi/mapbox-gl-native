package com.mapbox.mapboxsdk.testapp.activity.maplayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.mapboxsdk.testapp.R;

public class BottomSheetActivity extends AppCompatActivity {

  private final static String TAG_MAIN_FRAGMENT = "com.mapbox.mapboxsdk.fragment.tag.main";
  private final static String TAG_BOTTOM_FRAGMENT = "com.mapbox.mapboxsdk.fragment.tag.bottom";

  private BottomSheetBehavior bottomSheetBehavior;
  private boolean bottomSheetFragmentAdded;
  private boolean mainFragmentAdded;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bottom_sheet);

    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    bottomSheetBehavior.setPeekHeight((int) (64 * getResources().getDisplayMetrics().density));
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

    findViewById(R.id.fabFragment).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleMainMapFragment();
      }
    });

    findViewById(R.id.fabBottomSheet).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        toggleBottomSheetMapFragment();
      }
    });
  }

  private void toggleMainMapFragment() {
    if (!mainFragmentAdded) {
      addMainMapFragment();
    } else {
      removeMainMapFragment();
    }
    mainFragmentAdded = !mainFragmentAdded;
  }

  private void toggleBottomSheetMapFragment() {
    if (!bottomSheetFragmentAdded) {
      addBottomSheetMapFragment();
    } else {
      removeBottomSheetFragment();
    }
    bottomSheetFragmentAdded = !bottomSheetFragmentAdded;
  }

  private void addMainMapFragment() {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
    if (fragment == null) {
      fragmentTransaction.add(R.id.fragment_container, SupportMapFragment.newInstance(), TAG_MAIN_FRAGMENT);
    } else {
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.commit();
  }

  private void removeMainMapFragment() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
    if (fragment != null) {
      getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }
  }

  private void addBottomSheetMapFragment() {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.fragment_container_bottom, SupportMapFragment.newInstance(), TAG_BOTTOM_FRAGMENT);
    fragmentTransaction.commit();
  }

  private void removeBottomSheetFragment() {
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_BOTTOM_FRAGMENT);
    if (fragment != null) {
      getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }
  }
}
