package br.com.amandaluz.cielotickets.feature.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.com.amandaluz.cielotickets.R
import br.com.amandaluz.cielotickets.databinding.FragmentHomeBinding
import br.com.amandaluz.cielotickets.ui.binding.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sellButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_events)
        }
        binding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }
    }
}

