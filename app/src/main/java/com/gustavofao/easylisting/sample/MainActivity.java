package com.gustavofao.easylisting.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.gustavofao.easylisting.EasyListing;
import com.gustavofao.easylisting.sample.Models.Author;
import com.gustavofao.easylisting.sample.Models.Book;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private EasyListing.EasyListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List data = getData(20);
        listView = (ListView) findViewById(R.id.ListMain);
        adapter = new EasyListing.EasyListAdapter(MainActivity.this, data);

        adapter.setOnItemClickListener(new EasyListing.OnItemClickListener() {
            @Override
            public void onItemClick(int position, long objIdentifier, Object data, View view) {
                Toast.makeText(MainActivity.this, String.format("Item on position %d with identifier %d and DataClass is %s", position, objIdentifier, data.getClass().getSimpleName()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onLongItemClick(int position, long objIdentifier, Object data, View view) {
                return true;
            }
        });

        adapter.setOnCustomItemClickListener(Book.class, new EasyListing.OnCustomItemClickListener() {
            @Override
            public void onItemClick(int position, Object data, View view) {
                CompoundButton cpb = (CompoundButton) view.findViewById(R.id.readed);
                cpb.toggle();
            }

            @Override
            public boolean onLongItemClick(int position, Object data, View view) {
                return true;
            }
        });

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

        listView.setAdapter(adapter);
    }

    public List<Object> getData(int quantity) {
        List<Object> data = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            Book book = new Book();
            book.setId(String.valueOf(150 + i));
            book.setReaded(i % 2 == 0);
            book.setTitle(String.format("Book %d", i + 1));
            book.setPublishDate(new Date());
            book.setImage("https://fbcdn-sphotos-c-a.akamaihd.net/hphotos-ak-xta1/v/t1.0-9/591_995104263870137_8327010569290645471_n.png?oh=96d5339d4d256c78e5395e4f2a1a0367&oe=57E6F28A&__gda__=1474675946_a2041a1b00c59ed51b0f275a44a749a4");

            Author author = new Author();
            author.setName(String.format("Author of book %s", book.getTitle()));
            book.setAuthor(author);

//            data.add(author);
            data.add(book);
        }
        return data;
    }
}
