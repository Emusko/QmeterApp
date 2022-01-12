package com.example.qmeter.service.model.remote.response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

interface PageComponent {
    val position: Int?
}

data class AuthenticationResponseModel(
    @SerializedName("pages") val pages: ArrayList<Page> = arrayListOf(),
    @SerializedName("general-settings") val generalSettings: GeneralSettings? = null,
    @SerializedName("mark_page_data") val markPageData: ArrayList<MarkPageData>? = arrayListOf(),
    @SerializedName("final_page_data") val finalPageData: FinalPageData? = null
) : Serializable {

    data class Page(
        @SerializedName("condition") val condition: Condition? = null,
        @SerializedName("properties") val properties: PageProperties? = null,
        @SerializedName("customer_data") val customerData: CustomerData? = null,
        @SerializedName("comment_data") val commentData: CommentData? = null,
        @SerializedName("sli_data") val sliData: SliData? = null,
        @SerializedName("custom_field_feedback_component") val customFieldFeedbackComponent: CustomFieldFeedbackComponent? = null
    ) : Serializable

    data class Condition(
        val identification: ArrayList<String> = arrayListOf(),
        val overall: Overall? = Overall()
    ) : Serializable

    data class Overall(
        val positive: ConditionOverallData? = ConditionOverallData(),
        val neutral: ConditionOverallData? = ConditionOverallData(),
        val negative: ConditionOverallData? = ConditionOverallData()
    ) : Serializable

    data class CustomFieldFeedbackComponent(
        override val position: Int? = null,
        val attrs: ArrayList<CustomFieldFeedback>? = arrayListOf()
    ): Serializable, PageComponent
    data class CustomFieldFeedback(
        val name: String? = null,
        val placeholder: HashMap<String, String>? = hashMapOf(),
        val label: HashMap<String, String>? = hashMapOf(),
        val position: Int? = null,
        val prefix: String? = null,
        val disable: Boolean? = null,
        val is_prefix: Boolean? = null,
        val type: String? = null,
        val select: ArrayList<SelectOption>? = null,
        val required: Boolean? = null,
        val default: String? = null,
        val is_show: Boolean? = null,
        val select_design: SelectDesign? = null,
        val label_text_color: ArrayList<Int>? = arrayListOf(),
        val label_bg_color: ArrayList<Int>? = arrayListOf(),
        val label_text_size: String? = null
    ): Serializable

    data class ConditionOverallData(
        @SerializedName("comment_data") val commentData: Boolean? = null,
        @SerializedName("customer_data") val customerData: Boolean? = null,
        @SerializedName("custom_fields") val customFields: Boolean? = null
    ) : Serializable

    data class PageProperties(
        @SerializedName("submit_button_txt_color") val submitButtonTxtColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("submit_button_bg_color") val submitButtonBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("is_submit_enabled") val isSubmitEnabled: Boolean? = false,
        @SerializedName("is_next_button_enabled") val isNextButtonEnabled: Boolean? = false,
        @SerializedName("next_button_txt_color") val nextButtonTxtColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("next_button_bg_color") val nextButtonBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("is_back_button_enabled") val isBackButtonEnabled: Boolean? = false,
        @SerializedName("back_button_txt_color") val backButtonTxtColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("back_button_bg_color") val backButtonBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("page_bg") val pageBg: ArrayList<Int> = arrayListOf(),
        @SerializedName("page_header") val pageHeader: ArrayList<Int> = arrayListOf(),
        @SerializedName("is_page_title") val isPageTitle: Boolean? = null,
        @SerializedName("page_title") val pageTitle: HashMap<String, String>? = hashMapOf(),
        @SerializedName("page_title_size") val pageTitleSize: String? = null,
        @SerializedName("page_title_text_color") val pageTitleTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("page_title_bg_color") val pageTitleBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("timeout") val timeout: Timeout? = Timeout(),
        @SerializedName("service_with_rate") val serviceWithRate: Boolean? = null
    ): Serializable

    data class FinalPageData(

        val isAllPageBg: Boolean? = null,
        val pageBg: ArrayList<Int> = arrayListOf(),
        val pageHeader: ArrayList<Int> = arrayListOf(),
        val sizes: ArrayList<String> = arrayListOf(),
        val positive: Reaction? = null,
        val negative: Reaction? = null,
        val neutral: Reaction? = null,
        val timeout: Timeout? = null

    ) : Serializable

    data class Reaction(
        @SerializedName("text") val text: Label? = Label(),
        @SerializedName("text_color") val textColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("text_bg_color") val textBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("text_size") val textSize: String? = null,
        @SerializedName("page_bg") val pageBg: ArrayList<Int> = arrayListOf()
    ) : Serializable

    data class Timeout(
        @SerializedName("enable") val enable: Boolean? = null,
        @SerializedName("time") val time: Int? = null
    ) : Serializable

    data class MarkPageData(

        @SerializedName("id") val id: Int? = null,
        @SerializedName("idx") val idx: String? = null,
        @SerializedName("name") val name: Label? = Label(),
        @SerializedName("title") val title: Label? = Label(),
        @SerializedName("bg_color") val bgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("skip_button_text_color") val skipButtonTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("skip_button_bg_color") val skipButtonBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("submit_button_text_color") val submitButtonTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("submit_button_bg_color") val submitButtonBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_icon_color") val rateIconColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("title_bg_color") val titleBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("title_text_color") val titleTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("title_size") val titleSize: String? = null,
        @SerializedName("required") val required: Boolean? = null,
        @SerializedName("is_title") val isTitle: Boolean? = null,
        @SerializedName("rate_icon") val rateIcon: String? = null,
        @SerializedName("is_single") val isSingle: Boolean? = null,
        @SerializedName("marks") val marks: ArrayList<Marks> = arrayListOf()

    ) : Serializable

    data class Marks(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("name") val name: Label? = Label(),
        @SerializedName("position") val position: Int? = null,
        @SerializedName("bg_color") val bgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("text_color") val textColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("mark_border_color") val markBorderColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("mark_selected_color") val markSelectedColor: ArrayList<Int> = arrayListOf()
    ) : Serializable

    data class Properties(
        @SerializedName("page_background") val pageBackground: ArrayList<Int> = arrayListOf(),
        @SerializedName("dimmer_color") val dimmerColor: ArrayList<String> = arrayListOf(),
        @SerializedName("dimmer_opacity") val dimmerOpacity: Double? = null,
        @SerializedName("animated_title_size") val animatedTitleSize: String? = null,
        @SerializedName("language_label_size") val languageLabelSize: String? = null,
        @SerializedName("show_flags") val showFlags: Boolean? = null,
        @SerializedName("show_smile") val showSmile: Boolean? = null,
        @SerializedName("show_labels") val showLabels: Boolean? = null,
        @SerializedName("language_label_style") val languageLabelStyle: ArrayList<String> = arrayListOf(),
        @SerializedName("title_color") val titleColor: ArrayList<String> = arrayListOf(),
        @SerializedName("smiley_color") val smileyColor: ArrayList<String> = arrayListOf(),
        @SerializedName("smiley_size") val smileySize: String? = null,
        @SerializedName("background_image_url") val backgroundImageUrl: String? = null,
        @SerializedName("single") val single: String? = null,
        @SerializedName("default_lang") val defaultLang: String? = null,
        @SerializedName("language_label_color") val languageLabelColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("language_label_bg_color") val languageLabelBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("language_label_border_color") val languageLabelBorderColor: ArrayList<Int> = arrayListOf()
    ) : Serializable


    data class SliData(
        @SerializedName("is_component_title") val isComponentTitle: Boolean? = null,
        @SerializedName("component_title") val componentTitle: HashMap<String, String>? = null,
        @SerializedName("component_title_text_color") val componentTitleTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("component_title_size") val componentTitleSize: String? = null,
        @SerializedName("component_title_bg_color") val componentTitleBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("position") override val position: Int? = null,
        @SerializedName("show_rate_labels") val showRateLabels: Boolean? = null,
        @SerializedName("attrs") val attrs: SliAttrs? = null
    ) : PageComponent, Serializable

    data class SliAttrs(
        @SerializedName("service") val service: ArrayList<SliService> = arrayListOf(),
        @SerializedName("service_name_size") val serviceNameSize: String? = null
    ) : Serializable

    data class RateOptions(

        @SerializedName("id") val id: String? = null,
        @SerializedName("name") val name: String? = null,
        @SerializedName("label") val label: HashMap<String, String>? = hashMapOf(),
        @SerializedName("bg_color") val bgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("markpage_id") val markpageId: Int? = null,
        @SerializedName("markpage_idx") val markpageIdx: String? = null,
        @SerializedName("text_color") val textColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_bg_color") val rateBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_icon_color") val rateIconColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_selected_color") val rateSelectedColor: ArrayList<Int> = arrayListOf()

    ) : Serializable

    data class SliService(

        @SerializedName("id") val id: Int? = null,
        @SerializedName("name") val name: HashMap<String, String>? = hashMapOf(),
        @SerializedName("bg_color") val bgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("text_color") val textColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_bg_color") val rateBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("rate_icon_color") val rateIconColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("position") val position: Int? = null,
        @SerializedName("rate_options") val rateOptions: ArrayList<RateOptions> = arrayListOf(),
        @SerializedName("rate_selected_color") val rateSelectedColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("required") val required: Boolean? = null

    ) : Serializable

    data class Attrs(
        val name: String? = null,
        val placeholder: HashMap<String, String>? = hashMapOf(),
        val label: HashMap<String, String>? = hashMapOf(),
        val position: Int? = null,
        val prefix: String? = null,
        val disable: Boolean? = null,
        val is_prefix: Boolean? = null,
        val type: String? = null,
        val select: ArrayList<SelectOption>? = null,
        val required: Boolean? = null,
        val default: String? = null,
        val is_show: Boolean? = null,
        val select_design: SelectDesign? = null,
        val label_text_color: ArrayList<Int>? = arrayListOf(),
        val label_bg_color: ArrayList<Int>? = arrayListOf(),
        val label_text_size: String? = null
    ) : Serializable

    data class SelectDesign(
        val select: ArrayList<String>? = null,
        val value: String? = null
    ) : Serializable

    data class SelectOption(
        val id: String? = null,
        val option: Label? = null
    ) : Serializable

    data class CommentAttrs(
        val name: String? = null,
        val placeholder: HashMap<String, String>? = hashMapOf(),
        val label: HashMap<String, String>? = hashMapOf(),
        val type: String? = null,
        val required: Boolean? = false,
        val min_length: Int? = null,
        val max_length: Int? = null,
        val text_color: ArrayList<Int>? = arrayListOf(),
        val textarea_bg_color: ArrayList<Int>? = arrayListOf(),
        val label_text_color: ArrayList<Int>? = arrayListOf(),
        val label_text_size: String? = null,
        val label_bg_color: ArrayList<Int>? = arrayListOf()
    ) : Serializable

    data class CommentData(
        @SerializedName("position") override val position: Int? = null,
        @SerializedName("is_component_title") val isComponentTitle: Boolean? = null,
        @SerializedName("component_title") val componentTitle: Label? = Label(),
        @SerializedName("component_title_size") val componentTitleSize: String? = null,
        @SerializedName("component_title_bg_color") val componentTitleBgColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("component_title_text_color") val componentTitleTextColor: ArrayList<Int> = arrayListOf(),
        @SerializedName("attrs") val attrs: CommentAttrs? = null
    ) : PageComponent, Serializable


    data class FormErrors(
        @SerializedName("full_name_format") val fullNameFormat: Label? = null,
        @SerializedName("email_format") val emailFormat: Label? = null,
        @SerializedName("phone_format") val phoneFormat: Label? = null,
        @SerializedName("number_format") val numberFormat: Label? = null,
        @SerializedName("required") val required: Label? = null,
        @SerializedName("too_short") val tooShort: Label? = null,
        @SerializedName("too_long") val tooLong: Label? = null
    ) : Serializable

    data class CustomerData(

        @SerializedName("attrs") val attrs: ArrayList<Attrs> = arrayListOf(),
        @SerializedName("position") override val position: Int? = null,
        @SerializedName("is_component_title") val is_component_title: Boolean? = null,
        @SerializedName("component_title_bg_color") val component_title_bg_color: ArrayList<Int> = arrayListOf(),
        @SerializedName("component_title_text_color") val component_title_text_color: ArrayList<Int> = arrayListOf(),
        @SerializedName("component_title") val component_title: Label? = Label(),
        @SerializedName("component_title_size") val component_title_size: String? = null

    ) : PageComponent, Serializable

    data class GeneralSettings(

        val logo_url: String? = null,
        val form_errors: FormErrors? = FormErrors(),
        val is_updated: Boolean? = null,
        val page_style_for_all: Boolean? = null,
        val is_kiosk_mode: Boolean? = null,
        val is_enable_policy: Boolean? = null,
        val policy_label: Label? = Label(),
        val policy_label_color: ArrayList<Int> = arrayListOf(),
        val terms_title: Label? = Label(),
        val terms_title_size: String? = null,
        val terms_description_text: String? = null,
        val submit_button_text: Label? = Label(),
        val next_button_text: Label? = Label(),
        val back_button_text: Label? = Label()

    ) : Serializable

    data class Label(val en: String? = null) : Serializable

}