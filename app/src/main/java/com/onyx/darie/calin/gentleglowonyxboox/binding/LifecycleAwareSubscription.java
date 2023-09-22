package com.onyx.darie.calin.gentleglowonyxboox.binding;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class LifecycleAwareSubscription<T> implements Application.ActivityLifecycleCallbacks {
    private Activity activity;
    private Observable<T> source$;
    private Consumer<T> onNext;
    private Disposable subscription;

    public LifecycleAwareSubscription(Activity activity, Observable<T> source$, Consumer<T> onNext) {
        this.activity = activity;
        this.source$ = source$;
        this.onNext = onNext;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity != this.activity) return;
        subscription = source$
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity != this.activity) return;
        Unsubscribe();
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activity != this.activity) return;
        Unsubscribe();
    }

    private void Unsubscribe() {
        if (subscription != null) {
            subscription.dispose();
            subscription = null;
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) { }

    @Override
    public void onActivityStarted(@NonNull Activity activity) { }


    @Override
    public void onActivityStopped(@NonNull Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) { }
}
