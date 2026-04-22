package com.example.news_summary.scheduler

import com.example.news_summary.domain.settings.repository.CollectionScheduleRepository
import com.example.news_summary.news.application.usecase.CollectNewsUseCase
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * ニュース収集スケジューラジョブ。
 * @Scheduled で定期実行し、有効なスケジュールに対して CollectNewsUseCase を呼び出す。
 * Quartz Job インターフェースも実装し、動的スケジューリングにも対応する。
 */
@Component
class NewsCollectionJob(
    private val collectionScheduleRepository: CollectionScheduleRepository,
    private val collectNewsUseCase: CollectNewsUseCase
) : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 定期実行（毎時0分に実行）。
     * 有効なスケジュールを持つユーザーに対してニュース収集を実行する。
     * 各ユーザーのcron式との照合は簡易的に行い、
     * 実際のcronスケジューリングはQuartzに委譲する。
     */
    @Scheduled(cron = "0 0 * * * *")
    fun executeScheduled() {
        logger.info("ニュース収集ジョブ開始（定期実行）")
        executeAll()
    }

    /**
     * Quartz Job として実行される場合のエントリポイント。
     */
    override fun execute(context: JobExecutionContext?) {
        logger.info("ニュース収集ジョブ開始（Quartz）")
        executeAll()
    }

    /**
     * 有効なスケジュールを取得し、各ユーザーに対してニュース収集を実行する。
     */
    private fun executeAll() {
        val schedules = collectionScheduleRepository.findByEnabledTrue()
        logger.info("有効なスケジュール: ${schedules.size}件")

        schedules.forEach { schedule ->
            try {
                val result = collectNewsUseCase.execute(schedule.userId)
                logger.info(
                    "ニュース収集完了: userId=${schedule.userId.value}, " +
                    "saved=${result.savedCount}, skipped=${result.skippedCount}, errors=${result.errorCount}"
                )
            } catch (e: Exception) {
                logger.error(
                    "ニュース収集失敗: userId=${schedule.userId.value}, error=${e.message}", e
                )
            }
        }

        logger.info("ニュース収集ジョブ完了")
    }
}
