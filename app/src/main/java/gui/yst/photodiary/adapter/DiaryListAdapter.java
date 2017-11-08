package gui.yst.photodiary.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import gui.yst.photodiary.R;
import gui.yst.photodiary.model.Diary;

//Create Custom Adapter of Display Image and TextView in ListView http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/
//Android Delete Multiple Selected Items in ListView Tutorial http://www.androidbegin.com/tutorial/android-delete-multiple-selected-items-listview-tutorial/
//Faster Loading images http://www.coderzheaven.com/2013/09/01/faster-loading-images-gridviews-listviews-android-menory-caching-complete-implemenation-sample-code/
//*Filter http://codetheory.in/android-filters/
//*Filter http://stackoverflow.com/questions/2519317/how-to-write-a-custom-filter-for-listview-with-arrayadapter
public class DiaryListAdapter extends ArrayAdapter<Diary> implements Filterable{

    private class ViewHolder {
        TextView txtTitle;
        ImageView imageView;
        TextView extratxt;
    }

    private final Activity context;
    private List<Diary> diaries;
    private List<Diary> oridiaries = new ArrayList<>();
    private SparseBooleanArray mSelectedItemsIds;
    private ContactsFilter mContactsFilter;
    private final Object mLock = new Object();

    public DiaryListAdapter(Activity context, List<Diary> diaries) {
        super(context, R.layout.activity_diary_row, diaries);
        // TODO Auto-generated constructor stub
        mSelectedItemsIds = new SparseBooleanArray();
        this.context=context;
        this.diaries=diaries;
        cloneItems(diaries);
    }

    private void cloneItems(List<Diary> items) {
        for (Iterator iterator = items.iterator(); iterator
                .hasNext(); ) {
            Diary s = (Diary) iterator.next();
            oridiaries.add(s);
        }
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = view;
        ViewHolder holder;
        String dateFormat = "EEE, dd MMMM yyyy - h:mm a";
        SimpleDateFormat sdfdate = new SimpleDateFormat(dateFormat, Locale.UK);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(diaries.get(position).getDatetime());
        String time = sdfdate.format(calendar.getTime());

        if (view == null) {
            rowView = inflater.inflate(R.layout.activity_diary_row, null, true);

            holder = new ViewHolder();
            holder.txtTitle = (TextView) rowView.findViewById(R.id.item);
            holder.imageView = (ImageView) rowView.findViewById(R.id.icon);
            holder.extratxt = (TextView) rowView.findViewById(R.id.textView1);

            rowView.setTag(holder);
        }else
            holder = (ViewHolder) rowView.getTag();

        LoadImage loadImage = new LoadImage(holder.imageView);
        int j = 0;
        String num = Integer.toString(j);
        String tempPath = diaries.get(position).getLocation() + File.separator + "IMG_" + num + ".jpg";
        File tempFile = new File(tempPath);
        while(tempFile.exists()){
            j++;
            num = Integer.toString(j);
            tempPath = diaries.get(position).getLocation() + File.separator + "IMG_" + num + ".jpg";
            tempFile = new File(tempPath);
        }
        num = Integer.toString(j-1);
        tempPath = diaries.get(position).getLocation() + File.separator + "IMG_" + num + ".jpg";
        loadImage.execute(tempPath);
        holder.txtTitle.setText(diaries.get(position).getTitle());
        holder.extratxt.setText(time);
        return rowView;
    };

    @Override
    public int getCount()
    {
        return diaries.size();
    }

    @Override
    public Filter getFilter() {
        if (mContactsFilter == null)
            mContactsFilter = new ContactsFilter();
        return mContactsFilter;
    }
        private class ContactsFilter extends Filter {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                synchronized(mLock) {
                    final List<Diary> localDiaries = (List<Diary>) results.values;
                    notifyDataSetChanged();
                    clear();
                    for (Iterator iterator = localDiaries.iterator(); iterator
                            .hasNext();) {
                        Diary diary = (Diary) iterator.next();
                        add(diary);
                    }
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    results.values = oridiaries;
                    results.count = oridiaries.size();
                }else {
                    List<Diary> FilteredDiary = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();
                    String dateFormat = "EEE, dd MMMM yyyy - h:mm a";
                    SimpleDateFormat sdfdate = new SimpleDateFormat(dateFormat, Locale.UK);
                    for (Diary c : diaries) {
                        calendar.setTimeInMillis(c.getDatetime());
                        if (c.getTitle().toUpperCase().contains( constraint.toString().toUpperCase() ) || sdfdate.format(calendar.getTime()).toUpperCase().contains( constraint.toString().toUpperCase() )) {
                            FilteredDiary.add(c);
                        }
                    }

                    results.count = FilteredDiary.size();
                    results.values = FilteredDiary;
                }
                return results;
            }
        };

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        public LoadImage(ImageView imageView){
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inSampleSize = 20;
            return BitmapFactory.decodeFile(params[0], options);
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public void remove(Diary diary) {
        diaries.remove(diary);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, true);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}
