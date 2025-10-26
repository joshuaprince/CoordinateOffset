package com.jtprince.coordinateoffset.provider;

public abstract sealed class CoreOffsetProvider
    extends OffsetProvider
    permits ConstantOffsetProvider, RandomOffsetProvider, ZeroAtLocationOffsetProvider {

    public CoreOffsetProvider(String userDefinedProviderName) {
        super(userDefinedProviderName);
    }

    /**
     * Class name shown in the main pie chart on the bStats metrics page.
     * @return A String like "RandomOffsetProvider"
     */
    public abstract String getMetricsClassName();

    /**
     * Extra details shown when drilling down into a pie chart on the bStats metrics page.
     * @return Variable strings per offset provider.
     */
    public abstract String getMetricsDetails();
}
