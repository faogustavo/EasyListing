package com.gustavofao.easylisting.sample.Models;

import com.gustavofao.easylisting.Annotations.ComputedFieldValue;
import com.gustavofao.easylisting.Annotations.CustomDatePattern;
import com.gustavofao.easylisting.Annotations.InnerValues;
import com.gustavofao.easylisting.Annotations.RowIdentifier;
import com.gustavofao.easylisting.Annotations.FieldValue;
import com.gustavofao.easylisting.Annotations.RowView;
import com.gustavofao.easylisting.sample.R;

import java.util.Date;

@RowView(R.layout.list_item_book)
public class Book {

    @RowIdentifier
    private String id;

    @FieldValue(R.id.image)
    private String image;

    @FieldValue(R.id.readed)
    private boolean readed;

    private String title;

    @FieldValue(R.id.publishDate)
    @CustomDatePattern("dd/MM/yyyy")
    private Date publishDate;

    @InnerValues
    private Author author;

    public Book() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }

    @ComputedFieldValue(R.id.title)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }
}
