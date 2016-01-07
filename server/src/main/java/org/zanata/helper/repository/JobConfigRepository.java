/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.helper.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.helper.component.AppConfiguration;
import org.zanata.helper.model.JobConfig;
import org.zanata.helper.util.YamlUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import static org.apache.commons.io.Charsets.UTF_8;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class JobConfigRepository {
    private static final Logger log =
            LoggerFactory.getLogger(JobConfigRepository.class);

    private File configDirectory;

    @Inject
    protected AppConfiguration appConfiguration;

    @VisibleForTesting
    protected JobConfigRepository(File configDirectory) {
        this.configDirectory = configDirectory;
    }

    public JobConfigRepository() {
    }

    @PostConstruct
    public void postConstruct() {
        configDirectory = appConfiguration.getConfigDirectory();
    }

    public Optional<JobConfig> load(long id) {
        File latestJobConfig = latestJobConfig(id);
        if (latestJobConfig.exists()) {
            try (InputStream inputStream = new FileInputStream(
                    latestJobConfig)) {
                return Optional.of(YamlUtil.generateJobConfig(inputStream));
            } catch (IOException e) {
                log.error("error loading config file: {}", latestJobConfig, e);
            }
        }
        return Optional.empty();
    }

    // TODO we may have concurrent issue here
    public void persist(JobConfig jobConfig) {
        File jobConfigFolder = jobConfigFolder(jobConfig.getId());
        File latestConfigFile = latestJobConfig(jobConfig.getId());

        String incomingYaml = YamlUtil.generateYaml(jobConfig);

        boolean made = jobConfigFolder.mkdirs();
        try {
            if (!made && latestConfigFile.exists()) {
                String current =
                        FileUtils.readFileToString(latestConfigFile, UTF_8);
                if (current.endsWith(incomingYaml)) {
                    log.info("config has not changed");
                    return;
                }
                // back up current job config
                FileUtils.moveFile(latestConfigFile,
                        new File(jobConfigFolder, "-" + new Date().getTime()));
            }

            // write new job config
            FileUtils.write(latestConfigFile, incomingYaml, UTF_8);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }

    }

    public boolean delete(long id) {
        File jobConfigFolder = jobConfigFolder(id);
        try {
            FileUtils.deleteDirectory(jobConfigFolder);
            return true;
        } catch (IOException e) {
            log.error("failed to delete the job config folder: {}",
                    jobConfigFolder, e);
            return false;
        }
    }

    private File jobConfigFolder(long id) {
        return new File(configDirectory, id + "");
    }

    private File latestJobConfig(long id) {
        return new File(jobConfigFolder(id), "current.yaml");
    }

    private static File latestJobConfig(File jobConfigFolder) {
        return new File(jobConfigFolder, "current.yaml");
    }


    public List<JobConfig> getHistory(long id) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * Our job id is incremental so the largest number in the config directory
     * will be the largest job id.
     *
     * @return largest job id or 0 if there is no job yet
     * @see org.zanata.helper.model.JobIDGenerator
     */
    public long largestStoredJobId() {
        File[] jobConfigFolders = configDirectory.listFiles(File::isDirectory);
        Optional<String> largestJob =
                Arrays.stream(jobConfigFolders)
                        .map(File::getName)
                        .sorted(Collections.reverseOrder())
                        .findFirst();
        if (largestJob.isPresent()) {
            return Long.parseLong(largestJob.get());
        }
        return 0;
    }

    public List<JobConfig> getAllJobs() {
        return Arrays.stream(configDirectory.listFiles(File::isDirectory))
                .map(JobConfigRepository::latestJobConfig)
                .filter(file -> file.exists() && file.canRead())
                .map(YamlUtil::generateJobConfig)
                .collect(Collectors.toList());
    }
}
