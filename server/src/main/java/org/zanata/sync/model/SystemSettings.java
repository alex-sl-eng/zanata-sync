package org.zanata.sync.model;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@NoArgsConstructor
public class SystemSettings implements Serializable {
    /**
     * Must have read write access
     * i.e /tmp/zanataHelperRoot
     */
    @NotEmpty
    private String storageDir = "/tmp";

    private boolean deleteJobDir = true;

    @NotNull
    private List<String> fieldsNeedEncryption = Lists.newArrayList("apiKey");

    public void updateSettings(boolean deleteJobDir,
            List<String> fieldsNeedEncryption) {
        this.deleteJobDir = deleteJobDir;
        this.fieldsNeedEncryption = fieldsNeedEncryption;
    }
}
