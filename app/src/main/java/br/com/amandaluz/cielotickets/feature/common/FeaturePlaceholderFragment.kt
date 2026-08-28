package br.com.amandaluz.cielotickets.feature.common

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.FragmentFeaturePlaceholderBinding
import br.com.amandaluz.cielotickets.ui.binding.viewBinding
import br.com.amandaluz.cielotickets.ui.state.StatePanelUiModel

class FeaturePlaceholderFragment : Fragment(R.layout.fragment_feature_placeholder) {
    private val binding by viewBinding(FragmentFeaturePlaceholderBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setTitle(requireArguments().requiredResource(ARG_TITLE))
        binding.statePanel.render(
            StatePanelUiModel.Message(
                title = getString(requireArguments().requiredResource(ARG_STATE_TITLE)),
                message = getString(requireArguments().requiredResource(ARG_STATE_MESSAGE)),
                iconRes = requireArguments().requiredResource(ARG_ICON),
            ),
        )
    }

    @StringRes
    @DrawableRes
    private fun Bundle.requiredResource(key: String): Int =
        getInt(key).also { resource ->
            require(resource != 0) { "Missing navigation resource argument: $key" }
        }

    private companion object {
        const val ARG_TITLE = "titleRes"
        const val ARG_STATE_TITLE = "stateTitleRes"
        const val ARG_STATE_MESSAGE = "stateMessageRes"
        const val ARG_ICON = "iconRes"
    }
}

