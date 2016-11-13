package com.julo.android.redditpix.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by julianlo on 1/13/16.
 */
public class EndlessRecyclerView extends RecyclerView {
    private boolean mLoading = false;
    private OnMoreItemsNeededListener mListener;
    private RecyclerView.AdapterDataObserver mObserver = new AdapterDataObserver() {
        private int mPreviousItemCount;

        @Override
        public void onChanged() {
            // In the case where the item count dropped (due to a big change in adapter data),
            // check if we need to load more data.
            int itemCount = getAdapter().getItemCount();
            if (itemCount < mPreviousItemCount) {
                // This is kind of a hack. Checking for the bottom hit immediately is too soon, as
                // the viewholders don't seem to be in the recyclerview yet. So we post a message
                // to check on the next message loop tick. This seems to be good enough.
                EndlessRecyclerView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        checkBottomHit();
                    }
                });
            }
            mPreviousItemCount = itemCount;
        }
    };

    public interface OnMoreItemsNeededListener {
        // Called when the recyclerview is below the threshold at which we need more items.
        // The return value indicates whether or not to put the recyclerview into loading mode.
        // While in loading mode, the recyclerview will refrain from requesting for more items
        // until loading mode is cleared (via notifyDoneLoading).
        boolean onMoreItemsNeeded();
    }

    public EndlessRecyclerView(Context context) {
        super(context);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void checkBottomHit() {
        // Don't do this if we have no items, as this
        // would indicate that no adapter is set or no data is available.
        if (!mLoading
                && (mListener != null)
                && (getAdapter() != null)
                && (getAdapter().getItemCount() > 0)
                && !canScrollVertically(1 /* down */)) {
            mLoading = mListener.onMoreItemsNeeded();
        }
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        checkBottomHit();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(mObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
    }

    public void setOnMoreItemsNeededListener(OnMoreItemsNeededListener listener) {
        mListener = listener;
    }

    public void notifyDoneLoading() {
        mLoading = false;
    }
}
