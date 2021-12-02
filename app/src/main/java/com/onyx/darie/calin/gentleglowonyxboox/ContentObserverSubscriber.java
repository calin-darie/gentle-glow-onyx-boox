package com.onyx.darie.calin.gentleglowonyxboox;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;

/**
 * Based on work by Mateusz Perlak Copyright 2016 - http://www.apache.org/licenses/LICENSE-2.0
 */
public abstract class ContentObserverSubscriber<T> implements ObservableOnSubscribe<T> {
    protected final WeakReference<ContentResolver> contentResolverRef;
    protected final Uri[] observedUris;
    private volatile boolean isObserverRegistered = false;

    public static <T> Observable<T> create(final ContentResolver resolver, final Uri[] observedUris, final Function<Uri, T> fetchStatusFun) {
        return Observable.defer(new Callable<ObservableSource<? extends T>>() {
            @Override
            public ObservableSource<? extends T> call() {
                ContentObserverSubscriber<T> contentObserverSubscriber = new ContentObserverSubscriber<T>(resolver, observedUris) {
                    @Override
                    protected T fetchItem(Uri itemUri) {
                        try {
                            return fetchStatusFun.apply(itemUri);
                        } catch (Exception e) {
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

                emitter.setDisposable(Disposables.fromAction(new Action() {
                    @Override
                    public void run() {
                        if (contentResolverRef.get() != null && isObserverRegistered) {
                            contentResolverRef.get().unregisterContentObserver(contentObserver);
                        }
                        isObserverRegistered = false;
                    }
                }));

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