package com.icthh.xm.ms.configuration.service.processors;

import com.icthh.xm.commons.config.domain.Configuration;
import com.icthh.xm.ms.configuration.domain.TenantAliasTree.TenantAlias;
import com.icthh.xm.ms.configuration.service.TenantAliasService;
import com.icthh.xm.ms.configuration.utils.ConfigPathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.icthh.xm.ms.configuration.domain.TenantAliasTree.TraverseRule.BREAK;
import static com.icthh.xm.ms.configuration.domain.TenantAliasTree.TraverseRule.CONTINUE;
import static com.icthh.xm.ms.configuration.utils.ConfigPathUtils.getPathInTenant;
import static com.icthh.xm.ms.configuration.utils.ConfigPathUtils.getTenantName;
import static com.icthh.xm.ms.configuration.utils.ConfigPathUtils.isUnderTenantFolder;
import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantAliasProcessor implements PublicConfigurationProcessor {

    private static final String EMPTY_STRING = "";
    private final TenantAliasService tenantAliasService;

    @Override
    public boolean isSupported(Configuration configuration) {
        return isUnderTenantFolder(configuration.getPath());
    }

    @Override
    public List<Configuration> processConfiguration(Configuration configuration,
                                                    Map<String, Configuration> originalStorage,
                                                    Map<String, Configuration> targetStorage) {
        List<Configuration> resultConfigurations = new ArrayList<>();
        String path = configuration.getPath();
        targetStorage.remove(path);

        Map<String, TenantAlias> tenants = tenantAliasService.getTenantAliasTree().getTenants();
        String tenantName = getTenantName(path).orElse(EMPTY_STRING);
        if (!tenants.containsKey(tenantName)) {
            return resultConfigurations;
        }

        tenants.get(tenantName).traverseChild((parent, child) -> {
            String pathInChildTenant = getPathInTenant(path, child.getKey());
            if (originalStorage.containsKey(pathInChildTenant)) {
                return BREAK;
            }
            resultConfigurations.add(new Configuration(pathInChildTenant, configuration.getContent()));
            return CONTINUE;
        });

        return resultConfigurations;
    }

}
