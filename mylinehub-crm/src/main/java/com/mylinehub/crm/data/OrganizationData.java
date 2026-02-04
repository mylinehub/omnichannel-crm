package com.mylinehub.crm.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.repository.CustomerRepository;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.FileCategoryRepository;
import com.mylinehub.crm.repository.MediaRepository;
import com.mylinehub.crm.repository.ProductRepository;

public class OrganizationData {

    // Base timeout constant (2 seconds)
    private static final long BASE_TIMEOUT_SECONDS = 2;

    // Organization Name -> Organization
    private static Map<String, Organization> allOrganizations = new ConcurrentHashMap<String, Organization>();

    // Key = ariApplicationName (stasis app), Value = routing info (org + domain + port)
    private static final Map<String, AriAppRouteDTO> ariAppRouteMap = new ConcurrentHashMap<>();

    // Lock
    private static final ReentrantLock lock = new ReentrantLock(false);

    public static Map<String, Organization> workWithAllOrganizationData(
            String organization, Organization details, String action, OrganizationWorkingDTO organizationWorkingDTO) {

        Map<String, Organization> toReturn = null;
        String app = null;

        while (true) {
            try {
                long timeout = lock.getQueueLength() + BASE_TIMEOUT_SECONDS;

                if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
                    try {
                        Organization current = null;

                        switch (action) {
                            case "recalculate-storage":
                                recalculateStorage(organization, organizationWorkingDTO);
                                break;

                            case "get-one":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    toReturn = new HashMap<>();
                                    toReturn.put(organization, current);
                                }
                                break;

                            case "get":
                                // IMPORTANT: snapshot (do not leak internal ref)
                                toReturn = new HashMap<>(allOrganizations);
                                break;

                            case "get-by-ariApplication":

                                if (organizationWorkingDTO != null && organizationWorkingDTO.getAriApplication() != null) {
                                    app = organizationWorkingDTO.getAriApplication().trim();
                                }

                                if (app != null && !app.isEmpty()) {
                                    AriAppRouteDTO route = ariAppRouteMap.get(app);
                                    if (route != null && route.getOrganization() != null) {
                                        String orgName = route.getOrganization();
                                        Organization org = allOrganizations.get(orgName);
                                        if (org != null) {
                                            toReturn = new HashMap<>();
                                            toReturn.put(orgName, org);
                                        }
                                    }
                                }
                                break;

                            case "update":

                                // 1) Remove old ARI mappings for this org
                                Organization oldOrg = allOrganizations.get(organization);
                                if (oldOrg != null && oldOrg.getAriApplication() != null) {
                                    for (String oldApp : oldOrg.getAriApplication()) {
                                        if (oldApp != null && !oldApp.isBlank()) {
                                            ariAppRouteMap.remove(oldApp.trim());
                                        }
                                    }
                                }

                                // 2) Update org
                                allOrganizations.put(organization, details);

                                // 3) Add fresh ARI mappings (aligned by index)
                                if (details != null && details.getAriApplication() != null) {

                                    List<String> apps = details.getAriApplication();
                                    List<String> domains = details.getAriApplicationDomain();
                                    List<String> ports = details.getAriApplicationPort();

                                    for (int i = 0; i < apps.size(); i++) {
                                        String stasisApplication = safeGet(apps, i);
                                        if (stasisApplication == null) continue;

                                        String domain = safeGet(domains, i);
                                        String port = safeGet(ports, i);

                                        ariAppRouteMap.put(stasisApplication,
                                                new AriAppRouteDTO(organization, stasisApplication, domain, port));
                                    }
                                }

                                break;

                            case "delete":

                                Organization old = allOrganizations.get(organization);
                                if (old != null && old.getAriApplication() != null) {
                                    for (String application : old.getAriApplication()) {
                                        if (application != null && !application.isBlank()) {
                                            ariAppRouteMap.remove(application.trim());
                                        }
                                    }
                                }

                                allOrganizations.remove(organization);
                                break;

                            case "increase-total-calls":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    current.setTotalCalls(current.getTotalCalls() + 1);
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            case "increase-file-upload-size":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    current.setCurrentUploadInMB(
                                            current.getCurrentUploadInMB()
                                                    + (organizationWorkingDTO.getCurrentFileSize() / (1024 * 1024)));
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            case "delete-file-upload-size":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    current.setCurrentUploadInMB(
                                            current.getCurrentUploadInMB()
                                                    - (organizationWorkingDTO.getCurrentFileSize() / (1024 * 1024)));
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            case "update-amount":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    if (organizationWorkingDTO.isCallOnMobile()) {
                                        current.setCallingTotalAmountLoaded(
                                                current.getCallingTotalAmountLoaded() + (organizationWorkingDTO.getAmount() * 2));
                                    } else {
                                        current.setCallingTotalAmountLoaded(
                                                current.getCallingTotalAmountLoaded() + organizationWorkingDTO.getAmount());
                                    }
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            case "update-whatsapp-amount":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    if (current.getWhatsAppMessageLimit() != -1) {
                                        current.setTotalWhatsAppMessagesAmountSpend(
                                                current.getTotalWhatsAppMessagesAmountSpend()
                                                        + organizationWorkingDTO.getAmount());
                                    }
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            case "deductAIAmount":
                                current = allOrganizations.get(organization);
                                if (current != null) {
                                    current.setCallingTotalAmountSpend(
                                            current.getCallingTotalAmountSpend() + organizationWorkingDTO.getDeductAIAmount());
                                } else {
                                    throw new Exception("Organization " + organization + " not found");
                                }
                                allOrganizations.put(organization, current);
                                break;

                            default:
                                break;
                        }

                        break; // exit retry loop after successful lock

                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println(
                            "Timeout acquiring lock for workWithAllOrganizationData, retrying for organization: "
                                    + organization);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return toReturn;
    }

    public static AriAppRouteDTO getAriRouteDTOByAriApplication(String ariApplication) {
        if (ariApplication == null) return null;
        String key = ariApplication.trim();
        if (key.isEmpty()) return null;

        try {
            long timeout = lock.getQueueLength() + BASE_TIMEOUT_SECONDS;
            if (lock.tryLock(timeout, TimeUnit.SECONDS)) {
                try {
                    return ariAppRouteMap.get(key);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ariAppRouteMap.get(key);
    }

    private static String safeGet(List<String> list, int idx) {
        if (list == null) return null;
        if (idx < 0 || idx >= list.size()) return null;
        String v = list.get(idx);
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private static void recalculateStorage(String organization, OrganizationWorkingDTO organizationWorkingDTO) throws Exception {

        ApplicationContext applicationContext = organizationWorkingDTO.getApplicationContext();
        Organization current = allOrganizations.get(organization);
        AtomicReference<Double> totalMBUsed = new AtomicReference<>(0.0);

        try {
            if (current != null) {

                ProductRepository productRepository = applicationContext.getBean(ProductRepository.class);
                Long totalProductImageSize = productRepository.getTotalImageSizeForOrganization(organization);
                if (totalProductImageSize != null) {
                    totalMBUsed.updateAndGet(v -> v + (totalProductImageSize / (1024 * 1024)));
                }

                CustomerRepository customerRepository = applicationContext.getBean(CustomerRepository.class);
                Long totalCustomerImageSize = customerRepository.getTotalImageSizeForOrganization(organization);
                if (totalCustomerImageSize != null) {
                    totalMBUsed.updateAndGet(v -> v + (totalCustomerImageSize / (1024 * 1024)));
                }

                EmployeeRepository employeeRepository = applicationContext.getBean(EmployeeRepository.class);
                List<Employee> allEmployees = employeeRepository.findAllByOrganization(organization);

                Long totalEmployeeImageSize = employeeRepository.getTotalImageSizeForOrganization(organization);
                if (totalEmployeeImageSize != null) {
                    totalMBUsed.updateAndGet(v -> v + (totalEmployeeImageSize / (1024 * 1024)));
                }

                FileCategoryRepository fileCategoryRepository = applicationContext.getBean(FileCategoryRepository.class);
                Long totalFileCategoryImageSize = fileCategoryRepository.getTotalImageSizeForOrganization(organization);
                if (totalFileCategoryImageSize != null) {
                    totalMBUsed.updateAndGet(v -> v + (totalFileCategoryImageSize / (1024 * 1024)));
                }

                MediaRepository mediaRepository = applicationContext.getBean(MediaRepository.class);
                Long totalWhatsAppMediaImageSize = mediaRepository.getTotalImageSizeForExtension(organization);
                if (totalWhatsAppMediaImageSize != null) {
                    totalMBUsed.updateAndGet(v -> v + (totalWhatsAppMediaImageSize / (1024 * 1024)));
                }

                allEmployees.forEach((employee) -> {
                    try {
                        Long totalEmployeeMediaImageSize = mediaRepository.getTotalImageSizeForExtension(employee.getExtension());
                        if (totalEmployeeMediaImageSize != null) {
                            totalMBUsed.updateAndGet(v -> v + ((totalEmployeeMediaImageSize / (1024 * 1024))));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                current.setCurrentUploadInMB(totalMBUsed.get());
                allOrganizations.put(organization, current);

            } else {
                throw new Exception("Organization given was null. Cannot calculate current storage.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static final class AriAppRouteDTO {
        private final String organization;
        private final String ariApplication;
        private final String domain;
        private final String port;

        public AriAppRouteDTO(String organization, String ariApplication, String domain, String port) {
            this.organization = organization;
            this.ariApplication = ariApplication;
            this.domain = domain;
            this.port = port;
        }

        public String getOrganization() { return organization; }
        public String getAriApplication() { return ariApplication; }
        public String getDomain() { return domain; }
        public String getPort() { return port; }

        @Override
        public String toString() {
            return "AriAppRouteDTO{org=" + organization + ", app=" + ariApplication + ", domain=" + domain + ", port=" + port + "}";
        }
    }
}
