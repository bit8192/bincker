package cn.bincker.modules.clash.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubReleasesInfo {
    private String id;
    @JsonProperty("tag_name")
    private String tagName;
    @JsonProperty("update_url")
    private String updateUrl;
    @JsonProperty("update_authenticity_token")
    private String updateAuthenticityToken;
    @JsonProperty("delete_url")
    private String deleteUrl;
    @JsonProperty("delete_authenticity_token")
    private String deleteAuthenticityToken;
    @JsonProperty("edit_url")
    private String editUrl;
}
