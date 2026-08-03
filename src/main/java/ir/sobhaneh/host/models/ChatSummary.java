//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host.models;

import com.google.gson.annotations.SerializedName;

public record ChatSummary(String name, @SerializedName("unread_count") int unreadCount) {
}
