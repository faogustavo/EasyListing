package com.gustavofao.easylisting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gustavofao.easylisting.Annotations.ComputedFieldValue;
import com.gustavofao.easylisting.Annotations.CustomDatePattern;
import com.gustavofao.easylisting.Annotations.FieldValue;
import com.gustavofao.easylisting.Annotations.InnerFieldValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class EasyListing {

    private static EasyListing instance;

    private ImageLoader imageLoader;
    private String dateFormat;

    private HashMap<Class<?>, CustomViewCreator> customView;

    private EasyListing() {
        instance = this;

        dateFormat = "yyyy-MM-dd HH:mm:ss";
        customView = new HashMap<>();
    }

    public static EasyListing getInstance() {
        if (instance == null)
            new EasyListing();
        return instance;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setCustomViewHandler(Class<?> cls, CustomViewCreator callback) {
        if (!customView.containsKey(cls))
            customView.put(cls, callback);
    }

    public void removeCustomViewHandler(Class<?> cls) {
        if (customView.containsKey(cls))
            customView.remove(cls);
    }

    public CustomViewCreator getCustomViewHandler(Class<?> cls) {
        if (customView.containsKey(cls))
            return customView.get(cls);
        return null;
    }

    public static View normalCreation(ViewGroup parent, Context context, Object item) {
        View convertView = LayoutInflater.from(context).inflate(ReflectionUtils.getLayoutIdForObject(item), parent, false);
        return normalCreation(convertView, parent, context, item);
    }

    public static View normalCreation(View convertView, ViewGroup parent, Context context, Object item) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(ReflectionUtils.getLayoutIdForObject(item), parent, false);

        adjustRowValuesForView(convertView, item);
        return convertView;
    }

    private static void adjustRowValuesForViewHolder(ViewHolder viewHolder, Object item) {
        adjustRowValuesForView(viewHolder.getView(), item);
    }

    private static void adjustRowValuesForView(View convertView, Object item) {
        List<Field> fields = ReflectionUtils.getFieldsForRow(item);
        for (Field f : fields) {
            setValueOfItem(f, item, convertView.findViewById(f.getAnnotation(FieldValue.class).value()));
        }

        List<Field> innerValues = ReflectionUtils.getInnerFieldsForRow(item);
        for (Field f : innerValues) {
            boolean oldAcessible = f.isAccessible();
            try {
                f.setAccessible(true);
                Object subItem = f.get(item);

                List<Field> innerFields = ReflectionUtils.getInnerFieldsForRow(subItem, item.getClass());
                for (Field infi : innerFields) {
                    setValueOfItem(infi, subItem, convertView.findViewById(infi.getAnnotation(InnerFieldValue.class).layoutRes()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                f.setAccessible(oldAcessible);
            }
        }

        List<Method> computedValues = ReflectionUtils.getComputedFieldsForRow(item);
        for (Method method : computedValues) {
            setValueOfItem(method, item, convertView.findViewById(method.getAnnotation(ComputedFieldValue.class).value()));
        }
    }

    private static void setValueOfItem(Object objValue, View view, Annotation... annotations) {
        String objValueAsString = String.valueOf(objValue);
        if (objValue instanceof Boolean) {
            if (view instanceof CompoundButton) {
                ((CompoundButton) view).setChecked(Boolean.parseBoolean(objValueAsString));
            }
        }
        else if (objValue instanceof String) {
            if (view instanceof TextView) {
                ((TextView) view).setText(objValueAsString);
            } else if (view instanceof ImageView) {
                if (EasyListing.getInstance().getImageLoader() != null)
                    EasyListing.getInstance().getImageLoader().loadImage((ImageView) view, objValueAsString);
            }
        }
        else if (objValue instanceof Integer) {
            int val = Integer.valueOf(objValueAsString);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(val);
            } else if (view instanceof TextView) {
                ((TextView) view).setText(String.valueOf(val));
            }
        } else if (objValue instanceof Date || objValue instanceof Calendar) {
            Date date;
            String datePattern = EasyListing.getInstance().getDateFormat();

            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof CustomDatePattern) {
                        datePattern = ((CustomDatePattern) annotation).value();
                    }
                }
            }

            if (objValue instanceof Date)
                date = (Date) objValue;
            else
                date = ((Calendar) objValue).getTime();

            if (view instanceof TextView) {
                ((TextView) view).setText(new SimpleDateFormat(datePattern).format(date));
            }
        }
    }

    private static void setValueOfItem(Method method, Object object, View view) {
        boolean oldAcessible = method.isAccessible();
        try {
            method.setAccessible(true);
            Object objValue = method.invoke(object);
            setValueOfItem(objValue, view, method.getAnnotations());
        } catch (Exception e) {
            Log.e(EasyListing.class.getSimpleName(), String.format("Failed to invoke method %s", method.getName()));
        } finally {
            method.setAccessible(oldAcessible);
        }
    }

    private static void setValueOfItem(Field field, Object object,  View view) {
        boolean oldAcessible = field.isAccessible();
        try {
            field.setAccessible(true);
            Object objValue = field.get(object);
            setValueOfItem(objValue, view, field.getAnnotations());
        }
        catch (IllegalAccessException e) {
            Log.e(EasyListing.class.getSimpleName(), String.format("Failed to set value of %s", field.getName()));
        }
        finally {
            field.setAccessible(oldAcessible);
        }
    }

    /**
     * INTERFACES
     */
    public interface ImageLoader {
        void loadImage(ImageView imageView, String url);
    }

    public interface CustomViewCreator<T> {
        View withoutViewCreated(View convertView, int position, ViewGroup parent, Context context, T data);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, long objIdentifier, Object data, View view);
        boolean onLongItemClick(int position, long objIdentifier, Object data, View view);
    }

    public interface OnCustomItemClickListener<T> {
        void onItemClick(int position, T data, View view);
        boolean onLongItemClick(int position, T data, View view);
    }

    public interface OnAfterViewCreation<T> {
        void afterViewCreation(View view, T data);
    }

    /**
     * SubClasses
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public View getView() {
            return itemView;
        }
    }

    /**
     * ADAPTERS
     */
    public static class EasyListAdapter extends ArrayAdapter {

        private final Context context;
        private List data;

        private LayoutInflater inflater;
        private OnItemClickListener onItemClickListener;
        private HashMap<Class<?>, OnCustomItemClickListener> onCustomItemClickListener;
        private HashMap<Class<?>, OnAfterViewCreation> onAfterViewCreation;

        public EasyListAdapter(Context context, List data) {
            super(context, android.R.layout.simple_list_item_1);
            this.onCustomItemClickListener = new HashMap<>();
            this.inflater = LayoutInflater.from(context);
            this.onAfterViewCreation = new HashMap<>();
            this.context = context;
            this.data = data;
        }

        public EasyListAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            this.onCustomItemClickListener = new HashMap<>();
            this.inflater = LayoutInflater.from(context);
            this.onAfterViewCreation = new HashMap<>();
            this.data = new ArrayList<>();
            this.context = context;
        }

        public void setData(List data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public void addData(List data) {
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        public List<Object> getData() {
            return getData();
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void setOnCustomItemClickListener(Class<?> cls, OnCustomItemClickListener listener) {
            if (this.onCustomItemClickListener.containsKey(cls))
                this.onCustomItemClickListener.remove(cls);
            this.onCustomItemClickListener.put(cls, listener);
        }

        public void removeOnCustomItemClickListener(Class<?> cls) {
            if (this.onCustomItemClickListener.containsKey(cls))
                this.onCustomItemClickListener.remove(cls);
        }

        public void setOnAfterViewCreation(Class<?> cls, OnAfterViewCreation listener) {
            if (this.onAfterViewCreation.containsKey(cls))
                this.onAfterViewCreation.remove(cls);
            this.onAfterViewCreation.put(cls, listener);
        }

        public void removeOnAfterViewCreation(Class<?> cls) {
            if (this.onAfterViewCreation.containsKey(cls))
                this.onAfterViewCreation.remove(cls);
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            Long value = Long.valueOf(position);

            Object item = getItem(position);
            Field field = ReflectionUtils.getRowIdentifierField(item);

            if (field != null) {
                boolean oldAcessible = field.isAccessible();
                try {
                    field.setAccessible(true);
                    Object obj = field.get(item);
                    if (obj instanceof String) {
                        try {
                            long val = Long.parseLong(String.valueOf(obj));
                            value = val;
                        } catch (Exception ex) {}
                    } else if (obj instanceof Long)
                        value = (long) obj;
                    else if (obj instanceof Integer)
                        value = Long.valueOf((int) obj);
                } catch (Exception e) {
                } finally {
                    field.setAccessible(oldAcessible);
                }
            }

            return value;
        }

        @Override
        public int getPosition(Object item) {
            return data.indexOf(item);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                Object item = getItem(position);
                EasyListing.CustomViewCreator customViewCreator = EasyListing.getInstance().getCustomViewHandler(item.getClass());

                if (customViewCreator != null) {
                    convertView = customViewCreator.withoutViewCreated(convertView, position, parent, context, item);
                } else {
                    convertView = inflater.inflate(ReflectionUtils.getLayoutIdForObject(item), parent, false);
                    adjustRowValuesForView(convertView, item);
                }

                final int finalPosition = position;
                final Object finalItem = item;
                final View finalConvertView = convertView;

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onCustomItemClickListener.containsKey(finalItem.getClass())) {
                            onCustomItemClickListener.get(finalItem.getClass()).onItemClick(finalPosition, finalItem, finalConvertView);
                        } else if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(finalPosition, getItemId(finalPosition), finalItem, finalConvertView);
                        }
                    }
                });

                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (onCustomItemClickListener.containsKey(finalItem.getClass())) {
                            return onCustomItemClickListener.get(finalItem.getClass()).onLongItemClick(finalPosition, finalItem, finalConvertView);
                        } else if (onItemClickListener != null) {
                            return onItemClickListener.onLongItemClick(finalPosition, getItemId(finalPosition), finalItem, finalConvertView);
                        }
                        return true;
                    }
                });

                if (onAfterViewCreation.containsKey(finalItem.getClass())) {
                    onAfterViewCreation.get(finalItem.getClass()).afterViewCreation(finalConvertView, finalItem);
                }

                return convertView;
            }
            catch (Exception ex) {
                Log.d(EasyListing.class.getName(), ex.getMessage());
                return convertView;
//                return super.getView(position, convertView, parent);
            }
        }
    }
}
