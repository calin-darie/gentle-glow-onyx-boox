package com.onyx.darie.calin.gentleglowonyxboox.binding;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;

/**
 * Based on work by Mateusz Perlak Copyright 2016 - http://www.apache.org/licenses/LICENSE-2.0
 */
public abstract class ContentObserverSubscriber<T> implements ObservableOnSubscribe<T> {
    protected final WeakReference<ContentResolver> contentResolverRef;
    protected final Uri[] observedUris;
    private volatile boolean isObserverRegistered = false;

    public static <T> Observable<T> create(final ContentResolver resolver, final Uri[] observedUris, final Function<Uri, T> fetchStatusFun) {
        return Observable.defer(new Supplier<ObservableSource<? extends T>>() {
            @Override
            public ObservableSource<? extends T> get() {
                ContentObserverSubscriber<T> contentObserverSubscriber = new ContentObserverSubscriber<T>(resolver, observedUris) {
                    @Override
                    protected T fetchItem(Uri itemUri) {
                        Log.d("ContentObserver", ">> content changed for URI " + itemUri);
                        try {
                            return fetchStatusFun.apply(itemUri);
                        } catch (Throwable throwable) {
                            return null;
                        }
                    }
                };
                return Observable.create(contentObserverSubscriber);
            }
        });
    }

    @Override
    public void subscribe(@NonNull final ObservableEmitter<T> emitter) {
        try {
            if (contentResolverRef.get() != null) {

                final ContentObserver contentObserver = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        if (!selfChange) {
                            try {
                                T item = fetchItem(uri);
                                if (item != null) {
                                    emitter.onNext(item);
                                }
                            } catch (Exception ex) {
                                emitter.onError(ex);
                            }
                        }
                    }
                };

                for (Uri uri: observedUris) {
                    contentResolverRef.get().registerContentObserver(uri, true, contentObserver);
                }
                isObserverRegistered = true;

                emitter.setDisposable(new Disposable() {
                    private boolean isDisposed;
                    @Override
                    public void dispose() {
                        if (contentResolverRef.get() != null && isObserverRegistered) {
                            contentResolverRef.get().unregisterContentObserver(contentObserver);
                        }
                        isObserverRegistered = false;
                        isDisposed = true;
                    }

                    @Override
                    public boolean isDisposed() {
                        return isDisposed;
                    }
                });

            }
        } catch (Exception ex) {
            emitter.onError(ex);
        }
    }

    public ContentObserverSubscriber(ContentResolver resolver, Uri[] observedUris) {
        this.contentResolverRef = new WeakReference<>(resolver);
        this.observedUris = observedUris;
    }

    protected abstract T fetchItem(Uri changeUri) throws Exception;

}