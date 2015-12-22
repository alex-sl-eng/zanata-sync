package org.zanata.helper.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.zanata.helper.util.CronHelper;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@ToString
public class SyncToZanata implements JobConfig {
    private Long id;
    private String name;
    private String description;
    private String sourceRepositoryUrl;
    private String zanataVersionUrl;

    @Setter
    private JobStatus lastJobStatus;

    /**
     * see http://en.wikipedia.org/wiki/Cron#CRON_expression
     */
    private String cron = CronHelper.CronType.FIVE_MINUTES.getExpression();

    public SyncToZanata(Long id, String name, String description,
            String sourceRepositoryUrl, String zanataVersionUrl, String cron) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sourceRepositoryUrl = sourceRepositoryUrl;
        this.zanataVersionUrl = zanataVersionUrl;
        this.cron = cron;
    }

    @Override
    public Type getType() {
        return Type.SYNC_TO_ZANATA;
    }
}
