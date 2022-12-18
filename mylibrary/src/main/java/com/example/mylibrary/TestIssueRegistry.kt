package com.example.mylibrary

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class TestIssueRegistry : IssueRegistry() {

    override val issues: List<Issue>
        get() {
            val lintList = mutableListOf<Issue>(
                NullCheckDetector.ISSUE
            )
            /*   ServiceLoader.load(LintSpi::class.java).forEach {
                   it.issue().forEach { issue ->
                       lintList.add(issue)
                   }
               }*/
            return lintList
        }

}