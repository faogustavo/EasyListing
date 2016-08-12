EasyListing [ ![Download](https://api.bintray.com/packages/faogustavo/maven/EasyListing/images/download.svg) ](https://bintray.com/faogustavo/maven/EasyListing/_latestVersion) [![License](https://img.shields.io/hexpm/l/plug.svg)]() [![MinSDK](https://img.shields.io/badge/minSdk-15-brightgreen.svg)]()
=================================================================================================================================================================

The easiest way to implement a list on Android. No more adapters! No more inflate populating and views! Let this lib make the hard work.

## INSTALL
Add this dependecy from jCenter to your gradle app level:
```gradle
compile 'com.gustavofao:EasyListing:1.0.2'
```

If the installation fails, add this line to your gradle top level:
```gradle
maven { url "http://dl.bintray.com/faogustavo/maven" }
```

## USAGE

### FIRST STEP - *Create your model and view*
All models to be convetd need to have two things and one optional.
* It need to have the Annotation @RowView with the layout resource id for that model as value.
* It need to have the @FieldValue annotation on your attributes setting the value of the id you want to populate.
** You can also use the @ComputedInnerValue to show from a method.
* **[OPTIONAL]** Add the @RowIdentifier on your id. By now it must be a long but soon I will make it to accept Strings or int.
** if you did not set this value, it will always be -1.

Book.Java
```java
import com.gustavofao.easylisting.Annotations.InnerValues;
import com.gustavofao.easylisting.Annotations.RowIdentifier;
import com.gustavofao.easylisting.Annotations.FieldValue;
import com.gustavofao.easylisting.Annotations.RowView;
import com.gustavofao.easylisting.sample.R;

import java.util.Date;

@RowView(R.layout.list_item_book)
public class Book {

    @RowIdentifier
    private long id;

    @FieldValue(R.id.image)
    private String image;

    @FieldValue(R.id.readed)
    private boolean readed;

    private String title;

    @FieldValue(R.id.publishDate)
    private Date publishDate;

    public Book() {}

    /**
     * GETTERS AND SETTERS
     */
     
     @ComputedFieldValue(R.id.title)
     public String getTitle() {
         return title;
     }
}

```

list_item_book.xml
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
xmlns:android="http://schemas.android.com/apk/res/android"
android:orientation="horizontal"
android:layout_width="match_parent"
android:layout_height="84dp"
android:gravity="center"
android:paddingLeft="16dp"
android:paddingStart="16dp"
android:paddingRight="16dp"
android:paddingEnd="16dp">

<ImageView
    android:id="@+id/image"
    android:layout_width="48dp"
    android:layout_height="48dp" />

<LinearLayout
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    android:orientation="vertical">

    <TextView
        android:id="@+id/title"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Title of book"
        android:textSize="18sp"
        android:maxLines="1"
        android:ellipsize="end"
        android:layout_marginLeft="8dp"
        android:layout_marginStart="8dp"
        android:layout_marginRight="8dp"
        android:layout_marginEnd="8dp"/>

</LinearLayout>

<android.support.v7.widget.SwitchCompat
    android:id="@+id/readed"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />


</LinearLayout>
```


### SECOND STEP - *Adapter*
On your Activity, add these two lines:

```java
private ListView listView;
private EasyListing.EasyListAdapter adapter;
```

And then instantiate they where you want. On the sample I use on the *onCreate()* method. Using just this your lists will be created.
```java
List data = /** GET YOUR DATA **/;
adapter = new EasyListing.EasyListAdapter(MainActivity.this, data);

listView = (ListView) findViewById(R.id.ListMain);
listView.setAdapter(adapter);
```

### THIRD STEP - *Clicks*
The clicks can be created by two different ways. The basic that get the click for whatever item was clicked ou the specific for the model.

To Define the simple click, just call the method *setOnItemClickListener* from the adapter with your Listener. The listener needs to be an instance of EasyListing.OnItemClickListener.
```java
adapter.setOnItemClickListener(new EasyListing.OnItemClickListener() {
    @Override
    public void onItemClick(int position, long objIdentifier, Object data, View view) {
        Toast.makeText(MainActivity.this, String.format("Item on position %d with identifier %d and DataClass is %s", position, objIdentifier, data.getClass().getSimpleName()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onLongItemClick(int position, long objIdentifier, Object data, View view) {
        // If you dont wanna to handle long click you need to return false
        // When returning false, the long click will not be fired and the normal click will
        return true;
    }
});
```

To create a specific click listener for the model, you should do call *setOnCustomItemClickListener* with the parameters the class you want to fire that specific listener and the listener.
```java
adapter.setOnCustomItemClickListener(Book.class, new EasyListing.OnCustomItemClickListener() {
    @Override
    public void onItemClick(int position, Object data, View view) {
        CompoundButton readed = (CompoundButton) view.findViewById(R.id.readed);
        readed.toggle();
    }

    @Override
    public boolean onLongItemClick(int position, Object data, View view) {
        return true;
    }
});
```

### OPTIONALS

#### Set date format for Date Attribute
By default, when you want to show a date the library will use a defaul pattern that is **yyyy-MM-dd HH:mm:ss**.
If you want to change it, you can change by using this command:
```java
EasyListing.getInstance().setDateFormat("dd/MM/yyyy HH:mm");
```

You can change by using the Annotation @RowIdentifier.
```java
@FieldValue(R.id.publishDate)
@CustomDatePattern("dd/MM/yyyy")
private Date publishDate;
```

#### Load images from URL
By default no image will be loaded from URL. If you want to make it possible just use this snippet bellow. In this sample I will use Picasso.
```java
EasyListing.getInstance().setImageLoader(new EasyListing.ImageLoader() {
    @Override
    public void loadImage(ImageView imageView, String url) {
        Picasso.with(imageView.getContext())
                .load(url)
                .into(imageView);
    }
});
```

#### After view cration listener
If you want to add some specific listeners on your view, you cand add a listener that receive a call when the view with the specified class is created.
This is applyied to the adapter to makes it more flexible.
```java
adapter.setOnAfterViewCreation(Book.class, new EasyListing.OnAfterViewCreation() {
    @Override
    public void afterViewCreation(View view, Object data) {
        CompoundButton cpb = (CompoundButton) view.findViewById(R.id.readed);
        cpb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(MainActivity.this, "Agora está marcado? " + (isChecked ? "sim" : "não"), Toast.LENGTH_SHORT).show();
            }
        });
    }
});
```

#### Manual row creation
If you have some very specific rule for a model, you can create a customViewHandle. To use Follow the snipper bellow.
```java
EasyListing.getInstance().setCustomViewHandler(Book.class, new EasyListing.CustomViewCreator<Book>() {
    @Override
    public View withoutViewCreated(View convertView, int position, ViewGroup parent, Context context, Book data) {
        // The normalCreation method use the default algorithm to create and populate the view
        convertView = EasyListing.normalCreation(convertView, parent, context, data);

        if (position % 2 == 0)
            convertView.setBackgroundColor(Color.rgb(225,225,225));

        return convertView;
    }
});
```

## License
    Copyright 2016 Gustavo Fão. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
