package org.telegramIunzhakov.messenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

@SuppressLint("MissingPermission")
public class GoogleLocationProvider implements ILocationServiceProvider {
    private FusedLocationProviderClient locationProviderClient;
    private SettingsClient settingsClient;

    @Override
    public void init(Context context) {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        settingsClient = LocationServices.getSettingsClient(context);
    }

    @Override
    public ILocationRequest onCreateLocationRequest() {
        return new GoogleLocationRequest(LocationRequest.create());
    }

    @Override
    public void getLastLocation(Consumer<Location> callback) {
        locationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.getException() != null) {
                return;
            }
            callback.accept(task.getResult());
        });
    }

    @Override
    public void requestLocationUpdates(ILocationRequest request, ILocationListener locationListener) {
        locationProviderClient.requestLocationUpdates(((GoogleLocationRequest) request).request, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationListener.onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.getMainLooper());
    }

    @Override
    public void removeLocationUpdates(ILocationListener locationListener) {
        locationProviderClient.removeLocationUpdates(new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                locationListener.onLocationChanged(locationResult.getLastLocation());
            }
        });
    }

    @Override
    public void checkLocationSettings(ILocationRequest request, Consumer<Integer> callback) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(((GoogleLocationRequest) request).request);

        settingsClient.checkLocationSettings(builder.build()).addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
                callback.accept(STATUS_SUCCESS);
            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        callback.accept(STATUS_RESOLUTION_REQUIRED);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        callback.accept(STATUS_SETTINGS_CHANGE_UNAVAILABLE);
                        break;
                }
            }
        });
    }

    @Override
    public IMapApiClient onCreateLocationServicesAPI(Context context, IAPIConnectionCallbacks connectionCallbacks, IAPIOnConnectionFailedListener failedListener) {
        return new GoogleApiClientImpl(new GoogleApiClient.Builder(ApplicationLoader.applicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        connectionCallbacks.onConnected(bundle);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        connectionCallbacks.onConnectionSuspended(i);
                    }
                })
                .addOnConnectionFailedListener(connectionResult -> failedListener.onConnectionFailed())
                .build());
    }

    @Override
    public boolean checkServices() {
        return PushListenerController.GooglePushListenerServiceProvider.INSTANCE.hasServices();
    }

    public final static class GoogleLocationRequest implements ILocationRequest {
        private LocationRequest request;

        private GoogleLocationRequest(LocationRequest request) {
            this.request = request;
        }

        @Override
        public void setPriority(int priority) {
            int outPriority;
            switch (priority) {
                default:
                case PRIORITY_HIGH_ACCURACY:
                    outPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                    break;
                case PRIORITY_BALANCED_POWER_ACCURACY:
                    outPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                    break;
                case PRIORITY_LOW_POWER:
                    outPriority = LocationRequest.PRIORITY_LOW_POWER;
                    break;
                case PRIORITY_NO_POWER:
                    outPriority = LocationRequest.PRIORITY_NO_POWER;
                    break;
            }
            request.setPriority(outPriority);
        }

        @Override
        public void setInterval(long interval) {
            request.setInterval(interval);
        }

        @Override
        public void setFastestInterval(long interval) {
            request.setFastestInterval(interval);
        }
    }

    public final static class GoogleApiClientImpl implements IMapApiClient {
        private GoogleApiClient apiClient;

        private GoogleApiClientImpl(GoogleApiClient apiClient) {
            this.apiClient = apiClient;
        }

        @Override
        public void connect() {
            apiClient.connect();
        }

        @Override
        public void disconnect() {
            apiClient.disconnect();
        }
    }
}
