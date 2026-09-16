package com.alikuxac.aliss.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alikuxac.aliss.data.repository.ConfigRepositoryImpl
import com.alikuxac.aliss.domain.usecase.GetConfigUseCase
import com.alikuxac.aliss.domain.usecase.SaveConfigUseCase
import com.alikuxac.aliss.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val repository = ConfigRepositoryImpl(requireContext().applicationContext)
        val getConfigUseCase = GetConfigUseCase(repository)
        val saveConfigUseCase = SaveConfigUseCase(repository)
        val factory = SettingsViewModelFactory(getConfigUseCase, saveConfigUseCase)
        val settingsViewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        settingsViewModel.config.observe(viewLifecycleOwner) { config ->
            binding.etEndpoint.setText(config.endpoint)
            binding.etBucket.setText(config.bucket)
            binding.etAccessKey.setText(config.accessKey)
            binding.etSecretKey.setText(config.secretKey)
            binding.switchService.isChecked = config.isServiceEnabled
        }

        binding.btnSave.setOnClickListener {
            val endpoint = binding.etEndpoint.text.toString().trim()
            val bucket = binding.etBucket.text.toString().trim()
            val accessKey = binding.etAccessKey.text.toString().trim()
            val secretKey = binding.etSecretKey.text.toString().trim()
            val isEnabled = binding.switchService.isChecked

            settingsViewModel.saveConfig(endpoint, bucket, accessKey, secretKey, isEnabled, requireContext().applicationContext)
            Toast.makeText(requireContext(), "Configuration saved successfully", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
