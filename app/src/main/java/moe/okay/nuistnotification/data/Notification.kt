package moe.okay.nuistnotification.data

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val title: String,
    val category: Category,
    val date: String,
    val url: String,
    val publisher: String,
) {
    @Serializable
    enum class Category(val displayName: String) {
        AcademicReports("学术报告"),
        BiddingInformation("招标信息"),
        MeetingNotices("会议通知"),
        PartyAndGovernmentAffairs("党政事务"),
        OrganizationAndPersonnel("组织人事"),
        ScientificResearch("科研信息"),
        AdmissionsAndEmployment("招生就业"),
        TeachingAndExams("教学考试"),
        InnovationAndEntrepreneurship("创新创业"),
        AcademicSeminars("学术研讨"),
        SpecialLectures("专题讲座"),
        CampusActivities("校园活动"),
        CollegeNews("学院动态"),
        Other("其他"),
    }
}