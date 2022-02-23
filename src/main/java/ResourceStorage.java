import java.util.HashMap;

public class ResourceStorage {
    private HashMap<String, Integer> resourcesMap = new HashMap<String, Integer>();
    private HashMap<String, HashMap<String, Integer>> resourcesAllocation = new HashMap<String, HashMap<String, Integer>>();

    public void setResourceCount(String resourceName, Integer resourceCount) {
        resourcesMap.put(resourceName, resourceCount);
    }

    public boolean reserveResources(String clientId, String[] nameCountPairs) {
        for (String nameCountPair: nameCountPairs) {
            String[] nameCountArr = nameCountPair.split(":");
            if (!canReserveResource(nameCountArr[0], Integer.parseInt(nameCountArr[1]))) {
                return false;
            }
        }

        for (String nameCountPair: nameCountPairs) {
            String[] nameCountArr = nameCountPair.split(":");
            String resourceName = nameCountArr[0];
            Integer resourceCount = Integer.parseInt(nameCountArr[1]);

            HashMap<String, Integer> updatedAllocation = resourcesAllocation.containsKey(resourceName) ? resourcesAllocation.get(resourceName) : new HashMap<String, Integer>();
            int allocatedCount = updatedAllocation.containsKey(clientId) ? updatedAllocation.get(clientId) + resourceCount : resourceCount;
            updatedAllocation.put(clientId, allocatedCount);
            resourcesAllocation.put(resourceName, updatedAllocation);
        }

        return true;
    }

    private boolean canReserveResource(String resourceName, Integer resourceCount) {
        HashMap<String, Integer> allocationForResource = resourcesAllocation.get(resourceName);
        Integer allocatedCount = 0;
        if (allocationForResource != null) {
            for (String clientId : allocationForResource.keySet()) {
                allocatedCount += allocationForResource.get(clientId);
            }
        }

        Integer allowedResourceCount = resourcesMap.getOrDefault(resourceName, 0) - allocatedCount;

        return allowedResourceCount >= resourceCount;
    }

    public void printState() {
        System.out.println("Node resources:");
        System.out.println(resourcesMap);
        System.out.println("Reserved resources:");
        System.out.println(resourcesAllocation);
    }
}
