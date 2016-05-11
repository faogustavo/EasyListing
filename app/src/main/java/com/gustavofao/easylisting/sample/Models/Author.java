package com.gustavofao.easylisting.sample.Models;

import com.gustavofao.easylisting.Annotations.FieldValue;
import com.gustavofao.easylisting.Annotations.InnerFieldValue;
import com.gustavofao.easylisting.Annotations.RowView;
import com.gustavofao.easylisting.sample.R;

public class Author {

    @InnerFieldValue(
            layoutRes = R.id.author,
            parent = Book.class
    )
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
