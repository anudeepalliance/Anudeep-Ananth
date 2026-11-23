package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BarComparisonItem
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.CopyToClipboardButton
import com.anudeepananth.projects.design.DataTable
import com.anudeepananth.projects.design.DataTableColumn
import com.anudeepananth.projects.design.EyebrowText
import com.anudeepananth.projects.design.HeadingText
import com.anudeepananth.projects.design.HorizontalBarComparison
import com.anudeepananth.projects.design.LabelText
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TagChip
import com.anudeepananth.projects.design.TagTone
import com.anudeepananth.projects.design.ThinDivider
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXSmall
import com.anudeepananth.projects.design.tableColumnWidthLarge
import com.anudeepananth.projects.design.tableColumnWidthMedium
import com.anudeepananth.projects.design.tableColumnWidthSmall
import com.anudeepananth.projects.design.tableColumnWidthTiny
import com.anudeepananth.projects.ocrolus.OcrolusAddOnProduct
import com.anudeepananth.projects.ocrolus.OcrolusOutreachMessage
import com.anudeepananth.projects.ocrolus.OcrolusRunResult
import com.anudeepananth.projects.ocrolus.OcrolusScoredAccount
import com.anudeepananth.projects.ocrolus.formatting.formatNumberWithOneDecimal

/** The run's top accounts in ranked order, looked up from the full list of scored accounts. */
fun OcrolusRunResult.topScoredAccountsInRankOrder(): List<OcrolusScoredAccount> {
    val scoredAccountsById = scoredAccounts.associateBy {
        it.account.accountId
    }
    return topAccountIds.mapNotNull {
        scoredAccountsById[it]
    }
}

/** The total priority a run computed for [product] across all accounts. */
fun OcrolusRunResult.priorityTotalFor(product: OcrolusAddOnProduct): Double = when (product) {
    OcrolusAddOnProduct.FRAUD_SHIELD -> fraudShieldPriorityTotal
    OcrolusAddOnProduct.CASHFLOW_INSIGHTS -> cashFlowInsightsPriorityTotal
    OcrolusAddOnProduct.INCOME_VERIFY -> incomeVerifyPriorityTotal
}

/** Card naming the product a run picked, with bars comparing the three products' total priority. */
@Composable
fun ChosenProductCard(result: OcrolusRunResult, chosenProduct: OcrolusAddOnProduct, modifier: Modifier = Modifier) {
    ContentCard(modifier = modifier.fillMaxWidth()) {
        EyebrowText("Product to cross-sell")
        AddSpace(spacingSmall)
        HeadingText(chosenProduct.displayName)
        AddSpace(spacingXSmall)
        SecondaryText("Total priority across all 50 accounts, per product. The highest total wins.")
        AddSpace(spacingDefault)
        HorizontalBarComparison(
            items = OcrolusAddOnProduct.entries.map { product ->
                BarComparisonItem(
                    label = product.displayName,
                    value = result.priorityTotalFor(product),
                    valueLabel = formatNumberWithOneDecimal(result.priorityTotalFor(product)),
                    isHighlighted = product == chosenProduct,
                )
            },
        )
    }
}

/** Table of the ranked top accounts for the chosen product. */
@Composable
fun TopAccountsTable(result: OcrolusRunResult, chosenProduct: OcrolusAddOnProduct, modifier: Modifier = Modifier) {
    val topAccountColumns = listOf<DataTableColumn<OcrolusScoredAccount>>(
        DataTableColumn("Rank", tableColumnWidthTiny) { scoredAccount ->
            SubheadingText(scoredAccount.overallRank?.toString() ?: "–")
        },
        DataTableColumn("Account", tableColumnWidthLarge) { scoredAccount ->
            SubheadingText(scoredAccount.account.accountName)
        },
        DataTableColumn("Vertical", tableColumnWidthMedium) { scoredAccount ->
            BodyText(scoredAccount.account.vertical, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        DataTableColumn("Priority", tableColumnWidthSmall) { scoredAccount ->
            SubheadingText(formatNumberWithOneDecimal(scoredAccount.priorityFor(chosenProduct)))
        },
        DataTableColumn("Review", tableColumnWidthMedium) { scoredAccount ->
            if (scoredAccount.isFlaggedForManualReview) {
                TagChip("Manual review", tone = TagTone.WARNING)
            } else {
                SecondaryText("–")
            }
        },
    )
    DataTable(columns = topAccountColumns, rows = result.topScoredAccountsInRankOrder(), modifier = modifier)
}

/** One outreach email as a card: who it is for, the subject, the body with its line breaks, and a copy button. */
@Composable
fun OutreachEmailCard(
    outreachMessage: OcrolusOutreachMessage,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {},
) {
    ContentCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                EyebrowText("To ${outreachMessage.accountName.ifBlank { outreachMessage.accountId }}")
                AddSpace(spacingSmall)
                HeadingText(outreachMessage.finalSubject)
            }
            AddSpace(spacingMedium)
            CopyToClipboardButton(
                textToCopy = "Subject: ${outreachMessage.finalSubject}\n\n${outreachMessage.finalBody}",
                label = "Copy email",
            )
        }
        AddSpace(spacingDefault)
        ThinDivider()
        AddSpace(spacingDefault)
        BodyText(outreachMessage.finalBody)
        AddSpace(spacingDefault)
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TagChip("${outreachMessage.wordCount} words")
            if (outreachMessage.readiness.isNotBlank()) {
                LabelText(outreachMessage.readiness)
            }
        }
        extraContent()
    }
}
