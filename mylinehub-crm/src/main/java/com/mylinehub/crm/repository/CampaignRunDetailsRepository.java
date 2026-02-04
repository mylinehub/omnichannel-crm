package com.mylinehub.crm.repository;

import com.mylinehub.crm.entity.CampaignRunDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRunDetailsRepository extends JpaRepository<CampaignRunDetails, Long> {

    /**
     * Latest run for a campaign (by startedAt desc)
     */
    Optional<CampaignRunDetails> findTopByCampaignIdOrderByStartedAtDesc(Long campaignId);

    /**
     * All runs for a campaign (newest first)
     */
    List<CampaignRunDetails> findAllByCampaignIdOrderByStartedAtDesc(Long campaignId);

    /**
     * Distinct campaignIds that have runs (for reporting dropdown)
     */
    @Query(
            "select distinct d.campaignId " +
            "from CampaignRunDetails d " +
            "order by d.campaignId"
    )
    List<Long> findDistinctCampaignIds();

    /**
     * RunIds for a campaign (newest first)
     */
    @Query(
            "select d.id " +
            "from CampaignRunDetails d " +
            "where d.campaignId = :campaignId " +
            "order by d.startedAt desc"
    )
    List<Long> findRunIdsByCampaignId(@Param("campaignId") Long campaignId);

    /**
     * Fetch a run safely by campaign + runId (prevents mixing runs across campaigns)
     */
    Optional<CampaignRunDetails> findByIdAndCampaignId(Long id, Long campaignId);

    /**
     * Bulk fetch runs (useful for UI if you already have runIds)
     */
    List<CampaignRunDetails> findByIdIn(List<Long> ids);
    

    @Query(
            value = "select distinct on (campaign_id) campaign_id, campaign_name " +
                    "from campaign_run_details " +
                    "where organization = :org " +
                    "order by campaign_id, started_at desc",
            nativeQuery = true
    )
    List<Object[]> findLatestCampaignIdNamePairsForOrg(@Param("org") String organization);


    @Query(
            value = "select id, started_at " +
                    "from campaign_run_details " +
                    "where organization = :org and campaign_id = :campaignId " +
                    "order by started_at desc",
            nativeQuery = true
    )
    List<Object[]> findRunIdAndStartedAtForCampaignOrg(
            @Param("org") String organization,
            @Param("campaignId") Long campaignId
    );
}
